package screen.record.and.serve.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import screen.record.and.serve.server.AndroidWebServer;

public class NanoHTTPDServerService extends BaseForegroundService {
  private static final String TAG = NanoHTTPDServerService.class.getCanonicalName();

  public static final String ACTION_START_SERVER = "ACTION_START_SERVER";
  public static final String ACTION_STOP_SERVER = "ACTION_STOP_SERVER";

  private AndroidWebServer mAndroidWebServer;

  private static final String NOTIFICATION_CHANNEL_ID =
      NanoHTTPDServerService.class.getCanonicalName();

  public NanoHTTPDServerService() {
  }

  @Override public void onCreate() {
    super.onCreate();
    Log.d(TAG, "NanoHTTPDServerService service onCreate().");
    mAndroidWebServer = new AndroidWebServer();
    //startForegroundService();
    List<ActionDetail> list = new ArrayList<>(3);
    list.add(new ActionDetail(ACTION_START_SERVER, "Start Server"));
    list.add(new ActionDetail(ACTION_STOP_SERVER, "Stop Server"));
    list.add(new ActionDetail(ACTION_STOP_FOREGROUND_SERVICE, "Stop Service"));
    startForegroundService(list, "NanoHTTPDServerService", "Nano HTTPD Server Service");
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();
      if (action != null) {
        switch (action) {
          case ACTION_STOP_FOREGROUND_SERVICE:
            stopForegroundService();
            Log.d(TAG, "NanoHTTPDServerService service is stopped.");
            break;
          case ACTION_START_SERVER:
            Log.d(TAG, "start server clicked");
            try {
              mAndroidWebServer.start();
            } catch (IOException e) {
              e.printStackTrace();
            }
            break;
          case ACTION_STOP_SERVER:
            Log.d(TAG, "start stop clicked");
            mAndroidWebServer.stop();
            break;
        }
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override protected String getNotificationChannelId() {
    return NOTIFICATION_CHANNEL_ID;
  }

  protected void stopForegroundService() {
    super.stopForegroundService();
    Log.d(TAG, "Stop NanoHTTPDServerService service.");

    mAndroidWebServer.stop();
  }
}
