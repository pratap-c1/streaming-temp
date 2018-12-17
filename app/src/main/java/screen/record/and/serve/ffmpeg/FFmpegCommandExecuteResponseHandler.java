package screen.record.and.serve.ffmpeg;

import android.util.Log;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

public class FFmpegCommandExecuteResponseHandler implements FFmpegExecuteResponseHandler {
  private static final String TAG = FFmpegCommandExecuteResponseHandler.class.getCanonicalName();

  @Override public void onSuccess(String message) {
    Log.i(TAG, "onSuccess: " + message);
  }

  @Override public void onProgress(String message) {
    Log.d(TAG, "onProgress: " + message);
  }

  @Override public void onFailure(String message) {
    Log.e(TAG, "onFailure: " + message);
  }

  @Override public void onStart() {
    Log.i(TAG, "onStart");
  }

  @Override public void onFinish() {
    Log.i(TAG, "onFinish");
  }
}
