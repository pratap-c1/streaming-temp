package screen.record.and.serve.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.video.VideoQuality;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivitySpyDroidStreamBinding;
import screen.record.and.serve.services.RtspServer2;

public class SpyDroidStreamingActivity extends AppCompatActivity implements Session.Callback {
  private Session mSession;
  private final static String TAG = SpyDroidStreamingActivity.class.getCanonicalName();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivitySpyDroidStreamBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_spy_droid_stream);
    mSession = SessionBuilder.getInstance()
        .setCallback(this)
        .setSurfaceView(binding.surface)
        .setPreviewOrientation(90)
        .setContext(getApplicationContext())
        .setAudioEncoder(SessionBuilder.AUDIO_NONE)
        .setAudioQuality(new AudioQuality(16000, 32000))
        .setVideoEncoder(SessionBuilder.VIDEO_H264)
        .setVideoQuality(new VideoQuality(320, 240, 20, 500000))
        .build();
  }

  public void onPreviewStarted() {
    Log.d(TAG, "Preview started.");
  }

  @Override public void onSessionConfigured() {
    Log.d(TAG, "Preview configured.");
    // Once the stream is configured, you can get a SDP formated session description
    // that you can send to the receiver of the stream.
    // For example, to receive the stream in VLC, store the session description in a .sdp file
    // and open it with VLC while streming.
    Log.d(TAG, mSession.getSessionDescription());
    mSession.start();
  }

  @Override public void onSessionStarted() {
    Log.d(TAG, "Streaming session started.");
  }

  @Override public void onSessionStopped() {
    Log.d(TAG, "Streaming session stopped.");
  }

  @Override public void onBitrateUpdate(long bitrate) {
    // Informs you of the bandwidth consumption of the streams
    Log.d(TAG, "Bitrate: " + bitrate);
  }

  @Override public void onSessionError(int message, int streamType, Exception e) {
    // Might happen if the streaming at the requested resolution is not supported
    // or if the preview surface is not ready...
    // Check the Session class for a list of the possible errors.
    Log.e(TAG, "An error occured", e);
  }

  @Override protected void onResume() {
    super.onResume();
    mSession.startPreview();
    Intent serviceIntent = new Intent(this, RtspServer2.class);
    // start recording
    serviceIntent.setAction(RtspServer2.ACTION_START_FOREGROUND_SERVICE);
    //serviceIntent.setAction(RtspServer2.ACTION_STOP_FOREGROUND_SERVICE);
    startService(serviceIntent);
  }

  @Override protected void onPause() {
    super.onPause();
    mSession.stop();
  }
}
