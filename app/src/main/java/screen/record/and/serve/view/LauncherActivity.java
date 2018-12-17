package screen.record.and.serve.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import java.util.ArrayList;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityLauncherBinding;

public class LauncherActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityLauncherBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_launcher);
    binding.mainAct.setOnClickListener(v -> launch(this, MainActivity.class));
    binding.spyDroid.setOnClickListener(v -> launch(this, SpyDroidStreamingActivity.class));
    binding.exoPlayer.setOnClickListener(v -> launch(this, ExoActivity.class));
    binding.trial.setOnClickListener(v -> launch(this, TrialActivity.class));
    binding.rtsp.setOnClickListener(v -> launch(this, RtspActivity.class));
    binding.rtspClient.setOnClickListener(v -> launch(this, RTSPClientActivity.class));
    binding.webServer.setOnClickListener(v -> launch(this, WebServerActivity.class));
    binding.mediaProjectionDemo.setOnClickListener(v -> launch(this, MediaProjectionDemo.class));
    permissions();
  }

  private void permissions() {
    String[] permissions =
        { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO };
    Permissions.check(this/*context*/, permissions, "For recording"/*rationale*/, null/*options*/,
        new PermissionHandler() {
          @Override public void onGranted() {
            // do your task.
          }

          @Override public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            permissions();
          }
        });
  }

  private static void launchAndFinish(AppCompatActivity activity, Class<?> clazz) {
    activity.startActivity(new Intent(activity, clazz));
    activity.finish();
  }

  private static void launch(AppCompatActivity activity, Class<?> clazz) {
    activity.startActivity(new Intent(activity, clazz));
  }
}
