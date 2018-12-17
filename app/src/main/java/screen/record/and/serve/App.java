package screen.record.and.serve;

import android.app.Application;
import android.util.Log;
import com.blankj.utilcode.util.Utils;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import screen.record.and.serve.retrofit.SingleOkHttpClient;

public class App extends Application {
  private static final String TAG = App.class.getCanonicalName();

  @Override public void onCreate() {
    super.onCreate();
    Utils.init(this);
    SingleOkHttpClient.init();
    FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
    try {
      ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

        @Override public void onStart() {
          Log.d(TAG, "onStart");
        }

        @Override public void onFailure() {
          Log.d(TAG, "onFailure");
        }

        @Override public void onSuccess() {
          Log.d(TAG, "onSuccess");
        }

        @Override public void onFinish() {
          Log.d(TAG, "onFinish");
        }
      });
    } catch (FFmpegNotSupportedException e) {
      // Handle if FFmpeg is not supported by device
      Log.e(TAG, e.getLocalizedMessage());
      e.printStackTrace();
    }
  }
}
