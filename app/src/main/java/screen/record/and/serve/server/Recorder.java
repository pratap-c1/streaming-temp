package screen.record.and.serve.server;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.blankj.utilcode.util.ScreenUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Recorder {
  public static final String LIVE_STREAM = "live-video.mp4";
  private static final String TAG = Recorder.class.getCanonicalName();
  private MediaRecorder mMediaRecorder;
  private MediaProjectionManager mProjectionManager;
  private MediaProjection mMediaProjection;
  private boolean mRunning;
  private File mVideoDir;

  public Recorder(MediaProjectionManager mediaProjectionManager, File videoDir) {
    this.mProjectionManager = mediaProjectionManager;
    this.mMediaRecorder = new MediaRecorder();
    this.mVideoDir = videoDir;
  }

  public void setMediaProjection(MediaProjection mediaProjection) {
    this.mMediaProjection = mediaProjection;
  }

  private VirtualDisplay createVirtualDisplay() {
    if (mMediaProjection == null) return null;
    return mMediaProjection.createVirtualDisplay("MainScreen", ScreenUtils.getScreenWidth(),
        ScreenUtils.getScreenHeight(), ScreenUtils.getScreenDensityDpi(),
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
  }

  public void stopCapture() {
    if (mMediaProjection != null) mMediaProjection.stop();
    if (mRunning) mMediaRecorder.stop();
  }

  private InputStream initRecorder() throws IOException {
    File file = new File(mVideoDir, LIVE_STREAM);
    //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
    ParcelFileDescriptor[] parcelFileDescriptors = ParcelFileDescriptor.createPipe();
    ParcelFileDescriptor parcelRead = new ParcelFileDescriptor(parcelFileDescriptors[0]);
    ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(parcelFileDescriptors[1]);
    mMediaRecorder.setOutputFile(parcelWrite.getFileDescriptor());

    InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);

    mMediaRecorder.setVideoSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
    mMediaRecorder.setVideoFrameRate(30);
    try {
      Log.d(TAG, "preparing");
      mMediaRecorder.prepare();
      Log.d(TAG, "prepared");
      return is;
    } catch (IOException e) {
      Log.d(TAG, "error preparing");
      e.printStackTrace();
      return null;
    }
  }

  class Runnable1 implements Runnable {
    private InputStream is;
    private Socket socket;

    Runnable1(InputStream is, Socket socket) {
      this.is = is;
      this.socket = socket;
    }

    @Override public void run() {
      try {
        byte buffer[] = new byte[4];
        // Skip all atoms preceding mdat atom
        while (!Thread.interrupted()) {
          //while (is.read() != 'm') ;
          is.read(buffer, 0, 3);
          if (buffer[0] == 'd' && buffer[1] == 'a' && buffer[2] == 't') break;
          socket.getOutputStream().write(buffer, 0, 3);
        }
      } catch (IOException e) {
        Log.e(TAG, "Couldn't skip mp4 header :/");
        mMediaRecorder.stop();
        e.printStackTrace();
      }
    }
  }

  public boolean startRecord(Socket clientSocket) {
    if (mMediaProjection == null || mRunning) {
      return false;
    }
    mMediaRecorder = new MediaRecorder();
    try {
      InputStream is = initRecorder();
      if (is != null) {
        VirtualDisplay virtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        new Thread(new Runnable1(is, clientSocket)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    mRunning = true;
    return true;
  }
}
