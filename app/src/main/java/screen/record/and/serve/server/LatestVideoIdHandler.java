package screen.record.and.serve.server;

import android.os.Environment;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import screen.record.and.serve.models.LatestVideoIdModel;

public class LatestVideoIdHandler implements ResponseHandler {
  private static final String TAG = LatestVideoIdHandler.class.getCanonicalName();
  private static File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

  @Override public NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session)
      throws IOException {
    File f = new File(videoDir + File.separator + "source");
    String[] arr1 = f.list();
    if (arr1.length > 0) {
      int i1 = 0;
      for (String s : arr1) {
        try {
          int i = Integer.parseInt(s.substring(s.indexOf("am") + 2, s.indexOf(".mp4")));
          if (i > i1) i1 = i;
        } catch (NumberFormatException e) {
        }
      }
      if (i1 > 0) {
        return AndroidWebServer.jsonResponse(new LatestVideoIdModel(i1));
      }
    }
    return AndroidWebServer.jsonResponse(new LatestVideoIdModel(0));
  }
}
