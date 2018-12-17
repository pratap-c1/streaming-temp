package screen.record.and.serve.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DownloadVideo {
  @GET("live-stream3/{videoId}") Call<ResponseBody> downloadVideo(@Path("videoId") Integer videoId);
}
