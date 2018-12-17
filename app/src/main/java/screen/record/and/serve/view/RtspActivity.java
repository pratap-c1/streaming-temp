package screen.record.and.serve.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityRtspBinding;
import screen.record.and.serve.services.RtspServer2;

public class RtspActivity extends AppCompatActivity {
  private static String TAG = RtspActivity.class.getCanonicalName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityRtspBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_rtsp);
    binding.setActivity(this);
  }

  public void startServer() {
    Log.d(TAG, "startServer");
    Intent intent = new Intent(this, RtspServer2.class);
    intent.setAction(RtspServer2.ACTION_START_FOREGROUND_SERVICE);
    startService(intent);
  }
}
