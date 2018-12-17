package screen.record.and.serve.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.blankj.utilcode.util.NetworkUtils;
import java.util.List;
import screen.record.and.serve.R;

public abstract class BaseForegroundService extends Service {
  private static final String TAG = BaseForegroundService.class.getCanonicalName();
  public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

  abstract protected String getNotificationChannelId();

  protected void startForegroundService(List<ActionDetail> actionDetails, String notificationTitle,
      String channelName) {
    Log.d(TAG, "Start foreground service.");

    // Create notification default intent.
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    // Create notification builder.
    NotificationCompat.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel nc = new NotificationChannel(getNotificationChannelId(), channelName,
          NotificationManager.IMPORTANCE_HIGH);
      NotificationManager manager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      assert manager != null;
      manager.createNotificationChannel(nc);

      builder = new NotificationCompat.Builder(this, getNotificationChannelId());
    } else {
      // If earlier version channel ID is not used
      // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
      builder = new NotificationCompat.Builder(this);
    }
    // Make notification show big text.
    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
    bigTextStyle.setBigContentTitle(notificationTitle);
    bigTextStyle.bigText("IP: " + NetworkUtils.getIPAddress(true));
    // Set big text style.
    builder.setStyle(bigTextStyle);

    builder.setWhen(System.currentTimeMillis());
    builder.setSmallIcon(R.mipmap.ic_launcher);
    Bitmap largeIconBitmap =
        BitmapFactory.decodeResource(getResources(), android.R.drawable.stat_sys_speakerphone);
    builder.setLargeIcon(largeIconBitmap);
    // Make the notification max priority.
    builder.setPriority(Notification.PRIORITY_MAX);
    // Make head-up notification.
    builder.setFullScreenIntent(pendingIntent, true);

    // Add Play button intent in notification.

    // Ad launch button intent in notification.

    for (ActionDetail actionDetail : actionDetails) {
      builder.addAction(
          getNotificationAction(actionDetail.getAction(), android.R.drawable.ic_media_play,
              actionDetail.getMessage()));
    }
    // Build the notification.
    Notification notification = builder.build();

    // Start foreground service.
    startForeground(1, notification);
  }

  protected NotificationCompat.Action getNotificationAction(String action, int drawble,
      String title) {
    Intent launchIntent = new Intent(this, NanoHTTPDServerService.class);
    launchIntent.setAction(action);
    PendingIntent pendingLaunchIntent = PendingIntent.getService(this, 0, launchIntent, 0);
    return new NotificationCompat.Action(drawble, title, pendingLaunchIntent);
  }

  protected void stopForegroundService() {
    Log.d(TAG, "stopForegroundService");

    // Stop foreground service and remove the notification.
    stopForeground(true);

    // Stop the foreground service.
    stopSelf();
  }

  protected static class ActionDetail {
    private String action;
    private String message;

    public ActionDetail(String action, String message) {
      this.action = action;
      this.message = message;
    }

    public ActionDetail() {
    }

    public String getAction() {
      return action;
    }

    public void setAction(String action) {
      this.action = action;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
