package screen.record.and.serve.retrofit;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

public class SingleOkHttpClient {
  private static OkHttpClient client;

  public static void init() {

    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

    try {
      clientBuilder = untrustedClientBuilder(clientBuilder);
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    clientBuilder.connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
    //.addInterceptor(httpLoggingInterceptor)
    ;
    client = clientBuilder.build();
  }

  public static OkHttpClient getOkHttpClient() {
    return client;
  }

  private static OkHttpClient.Builder untrustedClientBuilder(OkHttpClient.Builder clientBuilder)
      throws NoSuchAlgorithmException, KeyManagementException {
    if (false) {
      final TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            @Override public X509Certificate[] getAcceptedIssuers() {
              X509Certificate[] cArrr = new X509Certificate[0];
              return cArrr;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            }
          }
      };
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      clientBuilder.sslSocketFactory(sslContext.getSocketFactory());
      HostnameVerifier hostnameVerifier = (hostname, session) -> true;
      clientBuilder.hostnameVerifier(hostnameVerifier);
    }
    return clientBuilder;
  }
}
