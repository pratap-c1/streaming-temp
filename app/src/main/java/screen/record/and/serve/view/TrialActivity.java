package screen.record.and.serve.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityTrialBinding;
import screen.record.and.serve.exo.RawH264DataSource;
import screen.record.and.serve.exo.RawH264DataSourceFactory;
import screen.record.and.serve.exo.RawH264ExtractorFactory;
import screen.record.and.serve.interfaces.RecordingInterface;
import screen.record.and.serve.services.MediaRecorderService;

public class TrialActivity extends AppCompatActivity implements RecordingInterface {
  public static final int EOF = -1;
  static final AtomicInteger increment = new AtomicInteger(1);
  private static final int REQUEST_CODE = 237;
  private static final String TAG = TrialActivity.class.getCanonicalName();
  private static final int WRITE_TO_FILE_DIRECT = 1;
  private static final int WRITE_TO_FILE_DESCRIPTION = 2;
  private static final int WRITE_TO_STREAM = 3;

  private final static String FFMPEG_COMMAND = "ffmpeg -i %1s -map 0 "
      // live-stream.mp4
      + "-codec:v libx264 " + /*"-codec:a aac " +*/ "-f ssegment -segment_list %2s "
      // playlist.m3u8
      + "-segment_list_flags +live -segment_time 10 " + "out%03d.ts";
  private static final int TWO_MB = 2 * 1024 * 1024;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  private MediaProjectionManager mProjectionManager;
  public static MediaRecorder mMediaRecorder;
  public static VirtualDisplay mVirtualDisplay;
  private MediaProjection mMediaProjection;
  private ActivityTrialBinding binding;
  private File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
  private MediaRecorderService mediaRecorderService;
  private boolean mBound = false;
  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      Log.d(TAG, "MediaRecorderService connected");
      MediaRecorderService.MediaRecorderServiceBinder binder =
          (MediaRecorderService.MediaRecorderServiceBinder) service;
      mediaRecorderService = binder.getService();
      MediaRecorderService.sMediaProjection = mMediaProjection;
      mBound = true;
    }

    @Override public void onServiceDisconnected(ComponentName arg0) {
      Log.d(TAG, "MediaRecorderService disconnected");
      mBound = false;
    }
  };

  private static String getFfmpegCommand(String soruceFilePath, String targetM3U8Path) {
    return FFMPEG_COMMAND.replace("%1s", soruceFilePath).replace("%2s", targetM3U8Path);
  }

  public static long copyMax2MB(final InputStream input, final OutputStream output,
      final byte[] buffer) throws IOException {
    long count = 0;
    int n;
    while (count <= TWO_MB && EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  public static String[] listOfFiles(String videoDir) {
    final int MAX_FILES = 10;
    File sourceDir = new File(videoDir + File.separator + "source");
    if (!sourceDir.exists()) {
      sourceDir.mkdirs();
    }
    String[] list = new String[MAX_FILES];
    for (int i = 1; i <= MAX_FILES; i++) {
      File f = new File(videoDir + File.separator + "source", "live-video" + i + ".ts");
      list[i - 1] = f.getAbsolutePath();
    }
    return list;
  }

  public void deleteAllFilesInADir() {
    FileUtils.deleteAllInDir(new File(videoDir, "source"));
  }

  @Override public void stopRecording() {
    stopCapture();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_trial);
    binding.setActivity(this);
    mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
  }

  @Override protected void onResume() {
    super.onResume();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      battery();
    }
  }

  public void startRecorderService() {
    captureRequest();
    Intent intent = new Intent(this, MediaRecorderService.class);
    intent.setAction(MediaRecorderService.ACTION_START_RECORDER);
    startService(intent);
  }

  public void bindRecorderService() {
    Intent intent = new Intent(this, MediaRecorderService.class);
    intent.setAction(MediaRecorderService.ACTION_MAKE_FOREGROUND_SERVICE);
    startService(intent);
  }

  @Override protected void onStop() {
    super.onStop();
    if (mBound) unbindService(mConnection);
    mBound = false;
  }

  @RequiresApi(Build.VERSION_CODES.M) private void battery() {
    String packageName = getPackageName();
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    assert pm != null;
    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
      Intent intent = new Intent();
      intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
      intent.setData(Uri.parse("package:" + packageName));
      startActivity(intent);
    }
  }

  public void captureRequest() {
    Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
    startActivityForResult(captureIntent, REQUEST_CODE);
  }

  public void stopCapture() {
    mMediaRecorder.stop();
    mVirtualDisplay.release();
  }

  public void stopCaptureFromBtn() {
    stopCapture();
    increment.set(1);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
      MediaRecorderService.sMediaProjection = mMediaProjection;
      //new Timer().schedule(new MyTimerTask(this), 0, 4000L);
    }
  }

  @Override public void project() throws IOException {
    mMediaRecorder = new MediaRecorder();
    //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);

    InputStream is = null;
    final int howToSave = WRITE_TO_FILE_DIRECT;
    switch (howToSave) {
      case WRITE_TO_FILE_DIRECT: {
        //mMediaRecorder.setOutputFile(new FileOutputStream(f).getFD());
        mMediaRecorder.setOutputFile(new FileOutputStream(
            new File(videoDir + File.separator + "source", "live-stream" + increment.get() + ".ts"))
            .getFD());
        increment.incrementAndGet();
      }
      break;
      case WRITE_TO_FILE_DESCRIPTION: {
        //mMediaRecorder.setOutputFile(new FileOutputStream(f).getFD());
        ParcelFileDescriptor[] parcelFileDescriptors = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor parcelRead = new ParcelFileDescriptor(parcelFileDescriptors[0]);
        ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(parcelFileDescriptors[1]);
        mMediaRecorder.setOutputFile(parcelWrite.getFileDescriptor());
        is = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);
      }
      break;
      case WRITE_TO_STREAM: {
        ParcelFileDescriptor[] parcelFileDescriptors = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor parcelRead = new ParcelFileDescriptor(parcelFileDescriptors[0]);
        ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(parcelFileDescriptors[1]);
        mMediaRecorder.setOutputFile(parcelWrite.getFileDescriptor());
        is = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);
      }
      break;
    }
    mMediaRecorder.setVideoSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
    mMediaRecorder.setVideoFrameRate(30);
    Log.d(TAG, "preparing");
    mMediaRecorder.prepare();
    Log.d(TAG, "prepared");
    mMediaRecorder.start();
    mVirtualDisplay =
        mMediaProjection.createVirtualDisplay("MainScreen", ScreenUtils.getScreenWidth(),
            ScreenUtils.getScreenHeight(), ScreenUtils.getScreenDensityDpi(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null,
            null);
    switch (howToSave) {
      case WRITE_TO_FILE_DIRECT: {
        //launchExoPlayer();
      }
      break;
      case WRITE_TO_FILE_DESCRIPTION: {
        new Thread(new MultipleFileCopyRunnable(videoDir.getAbsolutePath(), is, new Handler(),
            this)).start();
      }
      break;
      case WRITE_TO_STREAM: {
        playVideo(is);
      }
      break;
    }
    //new Handler().postDelayed(() -> {
    //  try {
    //    File hlsDir = new File(f.getParent() + File.separator + "hls");
    //    hlsDir.mkdirs();
    //    File playlistFile = new File(hlsDir, "playlist.m3u8");
    //    FFmpeg.getInstance(getApplicationContext()).execute(new String[] {
    //        getFfmpegCommand(f.getAbsolutePath(), playlistFile.getAbsolutePath())
    //    }, new FFmpegCommandExecuteResponseHandler());
    //  } catch (FFmpegCommandAlreadyRunningException e) {
    //    e.printStackTrace();
    //  }
    //}, 5000L);
  }

  private void playVideo(InputStream is) {
    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    //Create the player
    SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    binding.player.setPlayer(player);

    //RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
    //// This is the MediaSource representing the media to be played.
    //MediaSource videoSource =
    //    new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(
    //        Uri.parse("rtsp://192.168.200.2:8086"));
    RawH264DataSourceFactory rawH264DataSourceFactory = new RawH264DataSourceFactory(is);
    ExtractorMediaSource.Factory extractorMediaSourceFactory =
        new ExtractorMediaSource.Factory(rawH264DataSourceFactory);
    extractorMediaSourceFactory.setExtractorsFactory(new RawH264ExtractorFactory());
    MediaSource videoSource = extractorMediaSourceFactory.createMediaSource(null);

    player.prepare(videoSource);
    //auto start playing
    player.setPlayWhenReady(true);
  }

  static class MyTimerTask extends TimerTask {
    private static final String TAG = MyTimerTask.class.getCanonicalName();
    private WeakReference<RecordingInterface> stopRecording;

    MyTimerTask(RecordingInterface recordingInterface) {
      this.stopRecording = new WeakReference<>(recordingInterface);
    }

    @Override public void run() {
      Log.d(TAG, "run");
      try {
        if (increment.get() != 1) {
          if (stopRecording.get() != null) stopRecording.get().stopRecording();
        }
        if (stopRecording.get() != null) stopRecording.get().project();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static class MultipleFileCopyRunnable implements Runnable {
    private String videoDir;
    private InputStream is;
    private Handler handler;
    private WeakReference<RecordingInterface> stopRecordingWR;

    public MultipleFileCopyRunnable(String videoDir, InputStream is, Handler handler,
        RecordingInterface recordingInterface) {
      this.videoDir = videoDir;
      this.is = is;
      this.handler = handler;
      this.stopRecordingWR = new WeakReference<>(recordingInterface);
    }

    @Override public void run() {
      try {
        ok();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void ok() throws IOException {
      String[] listOfOutputFiles = listOfFiles(videoDir);

      for (int i = 0; i < listOfOutputFiles.length; i++) {
        copyMax2MB(is, new FileOutputStream(new File(listOfOutputFiles[i])),
            new byte[DEFAULT_BUFFER_SIZE]);
      }
      handler.post(() -> {
        if (stopRecordingWR.get() != null) {
          stopRecordingWR.get().stopRecording();
        }
      });
    }
  }

  static class MyRunnable1 implements Runnable {
    private InputStream is;
    private Socket clientSocket;

    public MyRunnable1(InputStream is, Socket clientSocket) {
      this.is = is;
      this.clientSocket = clientSocket;
    }

    @Override public void run() {
      try {
        readWritePipe();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void readWritePipe() throws IOException {
      while (!Thread.interrupted()) {
        byte[] bArr = new byte[4];
        while (is.read(bArr, 0, 4) != -1) {
          clientSocket.getOutputStream().write(bArr);
        }
      }
    }
  }

  static class MyRunnable2 implements Runnable {
    private InputStream is;

    public MyRunnable2(InputStream is) {
      this.is = is;
    }

    @Override public void run() {
      try {
        readWritePipe();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void readWritePipe() throws IOException {
      while (!Thread.interrupted()) {
        byte[] bArr = new byte[4];
        while (is.read(bArr, 0, 4) != -1) {
          new RawH264DataSource();
        }
      }
    }
  }
}
