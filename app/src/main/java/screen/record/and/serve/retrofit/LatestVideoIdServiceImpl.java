package screen.record.and.serve.retrofit;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import screen.record.and.serve.models.LatestVideoIdModel;

public class LatestVideoIdServiceImpl {
  public interface LVMCallback {
    void callback(boolean success, LatestVideoIdModel lvm);
  }

  public void callAPI(String baseUrl, final LVMCallback lvmCallback) {
    Call<LatestVideoIdModel> call = new RetrofitHelper().newMainServerRetrofit(baseUrl)
        .create(LatestVideoIdService.class)
        .getLatestVideoId();
    call.enqueue(new Callback<LatestVideoIdModel>() {
      @Override
      public void onResponse(Call<LatestVideoIdModel> call, Response<LatestVideoIdModel> response) {
        if (response.isSuccessful()) {
          LatestVideoIdModel lvm = response.body();
          lvmCallback.callback(true, lvm);
        } else {
          lvmCallback.callback(false, null);
        }
      }

      @Override public void onFailure(Call<LatestVideoIdModel> call, Throwable t) {
        Log.e("error", t.getLocalizedMessage());
        lvmCallback.callback(false, null);
      }
    });
  }
}
