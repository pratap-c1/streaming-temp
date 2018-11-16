package screen.record.and.serve.permissions;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import com.blankj.utilcode.util.PermissionUtils;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionToRecord {
  @RequiresApi(api = Build.VERSION_CODES.M) static public boolean isGranted() {
    return PermissionUtils.isGranted(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO);
  }

  @RequiresApi(api = Build.VERSION_CODES.M) static public void requestPermission(Context context) {
    PermissionUtils.permission(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO);
    PermissionUtils.PermissionActivity.start(context);
  }
}
