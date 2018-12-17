package screen.record.and.serve.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import screen.record.and.serve.models.LatestVideoIdModel;

public interface LatestVideoIdService {
  @GET("/latest-video-id") Call<LatestVideoIdModel> getLatestVideoId();
}
