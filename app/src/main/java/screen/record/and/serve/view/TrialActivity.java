package screen.record.and.serve.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.blankj.utilcode.util.ScreenUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityTrialBinding;

public class TrialActivity extends AppCompatActivity {
  private static final int REQUEST_CODE = 237;
  private static final String TAG = TrialActivity.class.getCanonicalName();
  private MediaProjectionManager mProjectionManager;
  private MediaRecorder mMediaRecorder;
  private MediaProjection mMediaProjection;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityTrialBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_trial);
    mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    mMediaRecorder = new MediaRecorder();
    mMediaProjection.createVirtualDisplay("MainScreen", ScreenUtils.getScreenWidth(),
        ScreenUtils.getScreenHeight(), ScreenUtils.getScreenDensityDpi(),
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
  }

  private void captureRequest() {
    Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
    startActivityForResult(captureIntent, REQUEST_CODE);
  }

  private void stopCapture() {
    mMediaRecorder.stop();
    mMediaProjection.stop();
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
      try {
        project();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void project() throws IOException {
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
    Log.d(TAG, "preparing");
    mMediaRecorder.prepare();
    Log.d(TAG, "prepared");
    mMediaRecorder.start();
  }

  static class MyRunnable implements Runnable {
    private InputStream is;
    private Socket clientSocket;

    public MyRunnable(InputStream is, Socket clientSocket) {
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
}
