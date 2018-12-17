package screen.record.and.serve.retrofit;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadVideoImpl {
  private final File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

  public interface DownloadStarted {
    void downloadStartedCallback();
  }

  public void callAPI(String baseUrl, int videoId,
      final DownloadVideoImpl.DownloadStarted downloadStarted) {
    Call<ResponseBody> call = new RetrofitHelper().newMainServerRetrofit(baseUrl)
        .create(DownloadVideo.class)
        .downloadVideo(videoId);
    call.enqueue(new Callback<ResponseBody>() {
      @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        new Thread(() -> {
          InputStream is = response.body().byteStream();
          try {
            IOUtils.copy(is, new FileOutputStream(videoDir.getAbsolutePath()
                + File.separator
                + "source"
                + File.separator
                + "my.mp4"));
            downloadStarted.downloadStartedCallback();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }).start();
      }

      @Override public void onFailure(Call<ResponseBody> call, Throwable t) {

      }
    });
  }
}
