package screen.record.and.serve.server;

import android.os.Environment;
import android.util.Log;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import screen.record.and.serve.models.BaseResponseModel;

public class AndroidWebServer extends NanoHTTPD {
  private final String TAG = AndroidWebServer.class.getCanonicalName();

  public AndroidWebServer(int port) {
    super(port);
  }

  public AndroidWebServer() {
    super(9001);
  }

  public AndroidWebServer(String hostname, int port) {
    super(hostname, port);
  }

  @Override public Response serve(IHTTPSession session) {
    Log.d(TAG, "Thread = " + Thread.currentThread().getName());
    String uri = session.getUri();
    Log.d(TAG, uri);

    ResponseHandler responseHandler = pickHandler(session);
    if (responseHandler == null) {
      return jsonResponse(BaseResponseModel.new404Error("Url not found"));
    } else {
      try {
        return responseHandler.handleResponse(session);
      } catch (IOException e) {
        e.printStackTrace();
        return jsonResponse(BaseResponseModel.new500Error(e.getCause()));
      }
    }
  }

  private ResponseHandler pickHandler(IHTTPSession ihttpSession) {
    if (ihttpSession.getUri().equals(UrlPaths.PATH_LIVE_VIDEO_STREAM)) {
      return new VideoHandler(
          new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
              Recorder.LIVE_STREAM));
    } else if (ihttpSession.getUri().equals(UrlPaths.PATH_LIVE_VIDEO_STREAM_2)) {
      try {
        return new StreamingVideoHandler(ServerThread.SERVER_IP);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    } else if (ihttpSession.getUri().startsWith(UrlPaths.PATH_LIVE_VIDEO_STREAM_3)) {
      return new SequentialVideoHandler();
    } else if (ihttpSession.getUri().equals(UrlPaths.PATH_LATEST_VIDEO_ID)) {
      return new LatestVideoIdHandler();
    } else if (ihttpSession.getUri().equals(UrlPaths.PATH_TEST)) {
      return new TestHandler();
    } else {
      return null;
    }
  }

  static public <T> Response jsonResponse(T data) {
    String json = new Gson().toJson(data);
    Response response = newFixedLengthResponse(json);
    response.addHeader("Content-Type", "application/json");
    return response;
  }
}
