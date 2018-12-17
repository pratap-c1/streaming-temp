package screen.record.and.serve.services;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;
import com.blankj.utilcode.util.ScreenUtils;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import screen.record.and.serve.Constants;
import screen.record.and.serve.ffmpeg.Commands;
import screen.record.and.serve.ffmpeg.FFmpegCommandExecuteResponseHandler;
import screen.record.and.serve.interfaces.RecordingInterface;

public class MediaRecorderService extends BaseForegroundService implements RecordingInterface {
  public static final String ACTION_START_RECORDER = "ACTION_START_RECORDER";
  public static final String ACTION_STOP_RECORDER = "ACTION_STOP_RECORDER";
  public static final String ACTION_MAKE_FOREGROUND_SERVICE = "ACTION_MAKE_FOREGROUND_SERVICE";
  public static final int EOF = -1;
  private static final String TAG = MediaRecorderService.class.getCanonicalName();
  private static final int REQUEST_CODE = 237;
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
  private static String CHANNEL_ID = MediaRecorder.class.getCanonicalName();
  final AtomicInteger increment = new AtomicInteger(1);
  // Binder given to clients
  private final IBinder mBinder = new MediaRecorderServiceBinder();
  private MediaProjectionManager mProjectionManager;
  private static MediaRecorder mMediaRecorder;
  public static MediaProjection sMediaProjection;
  private File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

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

  //public MediaProjectionManager getProjectionManager() {
  //  return mProjectionManager;
  //}
  //
  //public void setProjectionManager(MediaProjectionManager projectionManager) {
  //  this.mProjectionManager = projectionManager;
  //}

  @Override protected String getNotificationChannelId() {
    return CHANNEL_ID;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mBinder;
  }

  private void resetIncrementer() {
    increment.set(1);
  }

  @Override public void stopRecording() {
    stopCapture();
  }

  @Override public void project() throws IOException {
    //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder = new MediaRecorder();
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);

    InputStream is = null;
    final int howToSave = WRITE_TO_FILE_DIRECT;
    switch (howToSave) {
      case WRITE_TO_FILE_DIRECT: {
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
    sMediaProjection.createVirtualDisplay("MainScreen", ScreenUtils.getScreenWidth(),
        ScreenUtils.getScreenHeight(), ScreenUtils.getScreenDensityDpi(),
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    switch (howToSave) {
      case WRITE_TO_FILE_DIRECT: {
      }
      break;
      case WRITE_TO_FILE_DESCRIPTION: {
      }
      break;
      case WRITE_TO_STREAM: {
      }
      break;
    }
  }

  public void stopCapture() {
    if (mMediaRecorder != null) mMediaRecorder.stop();
  }

  public void stopCaptureAndReset() {
    stopCapture();
    resetIncrementer();
  }

  @Override public void onCreate() {
    super.onCreate();
    Log.d(TAG, "MediaRecorderService service onCreate().");
    //startForegroundService();
  }

  private void makeForeground() {
    List<ActionDetail> list = new ArrayList<>(3);
    list.add(new ActionDetail(ACTION_START_RECORDER, "Start Recorder"));
    list.add(new ActionDetail(ACTION_STOP_RECORDER, "Stop Recorder"));
    list.add(new ActionDetail(ACTION_STOP_FOREGROUND_SERVICE, "Stop Service"));
    mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    startForegroundService(list, "MediaRecorderService", "Media Recorder Server Service");
  }

  protected void stopForegroundService() {
    super.stopForegroundService();
    Log.d(TAG, "Stop MediaRecorderService service.");
    stopCapture();
    stopRecording();
    sMediaProjection = null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();
      if (action != null) {
        switch (action) {
          case ACTION_MAKE_FOREGROUND_SERVICE: {
            makeForeground();
            FFmpeg fFmpeg = FFmpeg.getInstance(getApplicationContext());
            try {
              //fFmpeg.execute(Commands.breakToHls(new File(Constants.SOURCE_DIR, "live-stream1.ts"),
              //    Constants.HLS_DIR, "my"), new FFmpegCommandExecuteResponseHandler());
              fFmpeg.execute(
                  Commands.breakToRtsp(new File(Constants.SOURCE_DIR, "live-stream1.ts")),
                  new FFmpegCommandExecuteResponseHandler());
            } catch (FFmpegCommandAlreadyRunningException e) {
              e.printStackTrace();
            }
          }
          break;
          case ACTION_STOP_FOREGROUND_SERVICE: {
            stopForegroundService();
            Log.d(TAG, "MediaRecorderService service is stopped.");
          }
          break;
          case ACTION_START_RECORDER: {
            Log.d(TAG, "start recorder clicked");
            stopRecording();
            new Timer().schedule(new MediaRecorderService.RepeatRecordingTimerTask(this, increment),
                0, TimeUnit.DAYS.toMillis(1));
          }
          break;
          case ACTION_STOP_RECORDER: {
            Log.d(TAG, "start recorder clicked");
          }
          break;
        }
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  static class RepeatRecordingTimerTask extends TimerTask {
    private static final String TAG = RepeatRecordingTimerTask.class.getCanonicalName();
    private WeakReference<RecordingInterface> stopRecording;
    private AtomicInteger increment;
    private FFmpeg fFmpeg;

    RepeatRecordingTimerTask(RecordingInterface recordingInterface, AtomicInteger increment) {
      this.increment = increment;
      this.stopRecording = new WeakReference<>(recordingInterface);
    }

    RepeatRecordingTimerTask(RecordingInterface recordingInterface, AtomicInteger increment,
        FFmpeg fFmpeg) {
      this.increment = increment;
      this.stopRecording = new WeakReference<>(recordingInterface);
      this.fFmpeg = fFmpeg;
    }

    @Override public void run() {
      Log.d(TAG, "run");
      try {
        if (increment.get() != 1) {
          if (stopRecording.get() != null) stopRecording.get().stopRecording();
        }
        if (stopRecording.get() != null) stopRecording.get().project();
        if (stopRecording.get() != null && fFmpeg != null) {
          // FFMpeg
          //fFmpeg.execute(Commands.breakToHls(new File(Constants.SOURCE_DIR, "live-stream1.ts"),
          //    Constants.HLS_DIR, "my"), new FFmpegCommandExecuteResponseHandler());
          fFmpeg.execute(Commands.breakToRtsp(new File(Constants.SOURCE_DIR, "live-stream1.ts")),
              new FFmpegCommandExecuteResponseHandler());
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (FFmpegCommandAlreadyRunningException e) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  public class MediaRecorderServiceBinder extends Binder {
    public MediaRecorderService getService() {
      // Return this instance of LocalService so clients can call public methods
      return MediaRecorderService.this;
    }
  }
}
