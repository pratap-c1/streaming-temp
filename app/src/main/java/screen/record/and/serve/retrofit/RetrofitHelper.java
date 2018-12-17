package screen.record.and.serve.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

  public Retrofit newMainServerRetrofit(String mainServerUrl) {
    return new Retrofit.Builder().client(SingleOkHttpClient.getOkHttpClient())
        .baseUrl(mainServerUrl.endsWith("/") ? mainServerUrl : mainServerUrl + "/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }

  public Retrofit newAnyUrlFileDownloaderRetrofit() {
    return new Retrofit.Builder().client(SingleOkHttpClient.getOkHttpClient())
        .baseUrl("http://google.com/")
        .build();
  }
}
