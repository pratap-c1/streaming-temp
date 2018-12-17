package screen.record.and.serve.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.blankj.utilcode.util.NetworkUtils;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityWebServerBinding;
import screen.record.and.serve.models.Data;
import screen.record.and.serve.services.NanoHTTPDServerService;

public class WebServerActivity extends AppCompatActivity {
  private ActivityWebServerBinding binding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_web_server);
    binding.setActivity(this);
    Data data = new Data();
    data.ipAddress = NetworkUtils.getIPAddress(true);
    binding.setData(data);
  }

  public void startServerService() {
    Intent intent = new Intent(this, NanoHTTPDServerService.class);
    intent.setAction(NanoHTTPDServerService.ACTION_START_SERVER);
    startService(intent);
  }

  public void stopServerService() {
    Intent intent = new Intent(this, NanoHTTPDServerService.class);
    intent.setAction(NanoHTTPDServerService.ACTION_STOP_FOREGROUND_SERVICE);
    startService(intent);
  }
}
