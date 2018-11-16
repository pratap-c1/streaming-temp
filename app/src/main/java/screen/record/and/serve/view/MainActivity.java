package screen.record.and.serve.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.blankj.utilcode.util.NetworkUtils;
import java.io.File;
import java.util.Collections;
import java.util.List;
import screen.record.and.serve.R;
import screen.record.and.serve.client.ClientThread;
import screen.record.and.serve.databinding.ActivityMainBinding;
import screen.record.and.serve.models.Data;
import screen.record.and.serve.permissions.PermissionToRecord;
import screen.record.and.serve.server.Recorder;
import screen.record.and.serve.server.ServerThread;
import screen.record.and.serve.services.MyForegroundService;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_CODE = 2323;
  private static String TAG = MainActivity.class.getCanonicalName();
  private ActivityMainBinding binding;
  private MediaProjectionManager mProjectionManager;
  private Recorder mRecorder;
  private ClientThread mClientThread;
  private boolean mRunning;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    Data data = new Data();
    data.ipAddress = NetworkUtils.getIPAddress(true);
    binding.setData(data);

    File videoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

    mRecorder = new Recorder(mProjectionManager, videoDir);

    new ServerThread(new Handler(), mRecorder);

    binding.recordToggle.setChecked(mRunning);
    binding.recordToggle.setOnCheckedChangeListener((compoundButton, isChecked) -> {
      Intent serviceIntent = new Intent(MainActivity.this, MyForegroundService.class);
      if (isChecked) {
        // start recording
        serviceIntent.setAction(MyForegroundService.ACTION_START_FOREGROUND_SERVICE);
        captureRequest();
      } else {
        serviceIntent.setAction(MyForegroundService.ACTION_STOP_FOREGROUND_SERVICE);
        stopCapture();
      }
      startService(serviceIntent);
    });

    binding.clientSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
      if (isChecked) {
        // start client
        File f = new File(videoDir, "live-video.mp4");
        mClientThread =
            new ClientThread(binding.serverIp.getText().toString(), true, getApplicationContext(),
                f, binding.playerView);
        new Thread(mClientThread).start();
      } else {
        //stop client
        if (mClientThread != null) {
          mClientThread.setConnected(false);
        }
      }
    });
  }

  @Override protected void onStart() {
    super.onStart();
    //launcher();
    //codeSnippet();
  }

  private void codeSnippet() {
    final String LAUNCH_APP = "LAUNCH_APP";
    final String CLOSE_APP = "CLOSE_APP";
    final String EXTRA_EVENT = "EXTRA_EVENT";

    // to launch Lemma App
    Intent startLemmaAppIntent = new Intent(Intent.ACTION_MAIN);
    startLemmaAppIntent.setComponent(
        new ComponentName(
            "com.lemma.advertisement", // package name of Lemma's app
            "com.lemma.advertisement.activities.LemmaMainActivity" // full class name of the activity Lemma will create
        ));
    startLemmaAppIntent.putExtra(EXTRA_EVENT, LAUNCH_APP);
    startActivity(startLemmaAppIntent);

    // to close Lemma App
    Intent closeLemmaAppIntent = new Intent(Intent.ACTION_MAIN);
    closeLemmaAppIntent.setComponent(
        new ComponentName(
            "com.lemma.advertisement", // package name of Lemma's app
            "com.lemma.advertisement.activities.LemmaMainActivity" // full class name of the activity Lemma will create
        ));
    closeLemmaAppIntent.putExtra(EXTRA_EVENT, CLOSE_APP);
    startActivity(closeLemmaAppIntent);
  }

  private void readIntent() {
    final String LAUNCH_APP = "LAUNCH_APP";
    final String CLOSE_APP = "CLOSE_APP";
    final String EXTRA_EVENT = "EXTRA_EVENT";

    Intent intent = getIntent();
    String extraEvent = intent.getStringExtra(EXTRA_EVENT);

    if (LAUNCH_APP.equals(extraEvent)) {
      // launch your activity which runs the advertisement
    } else if (CLOSE_APP.equals(extraEvent)) {
      // close your application gracefully
    }
  }

  private void launcher() {
    final PackageManager pm = getPackageManager();

    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
    Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

    for (ResolveInfo temp : appList) {

      Log.v("apps", "package and activity name = "
          + temp.activityInfo.packageName
          + "    "
          + temp.activityInfo.name);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (Build.VERSION_CODES.M > Build.VERSION.SDK_INT || PermissionToRecord.isGranted()) {
      // start screen record
      binding.recordToggle.setEnabled(true);
      Log.d(TAG, "permission granted");
    } else if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      binding.recordToggle.setEnabled(false);
      // request permission
      PermissionToRecord.requestPermission(this);
    }
  }

  @Override protected void onPause() {
    super.onPause();
  }

  private void captureRequest() {
    Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
    startActivityForResult(captureIntent, REQUEST_CODE);
  }

  private void stopCapture() {
    mRecorder.stopCapture();
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      MediaProjection mp = mProjectionManager.getMediaProjection(resultCode, data);
      if (mp != null) {
        mRecorder.setMediaProjection(mp);
      }
    }
  }
}
