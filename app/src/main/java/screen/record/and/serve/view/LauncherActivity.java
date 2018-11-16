package screen.record.and.serve.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityLauncherBinding;

public class LauncherActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityLauncherBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_launcher);
    binding.mainAct.setOnClickListener(v -> launchAndFinish(this, MainActivity.class));
    binding.spyDroid.setOnClickListener(
        v -> launchAndFinish(this, SpyDroidStreamingActivity.class));
    binding.exoPlayer.setOnClickListener(v -> launchAndFinish(this, ExoActivity.class));
  }

  private static void launchAndFinish(AppCompatActivity activity, Class<?> clazz) {
    activity.startActivity(new Intent(activity, clazz));
    activity.finish();
  }
}
