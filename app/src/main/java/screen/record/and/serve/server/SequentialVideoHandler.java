package screen.record.and.serve.server;

import android.os.Environment;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SequentialVideoHandler implements ResponseHandler {
  private static final String TAG = SequentialVideoHandler.class.getCanonicalName();
  private static File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

  @Override public NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session)
      throws IOException {
    String url = session.getUri();
    String[] arr = url.split("/");
    Integer count = null;
    try {
      count = Integer.parseInt(arr[arr.length - 1]);
    } catch (NumberFormatException e) {
    }
    File f;
    if (count == null) {
      f = new File(videoDir + File.separator + "source");
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
        if (i1 == 0) {
          // no valid files
          f = null;
        } else {
          count = i1;
          f = new File(videoDir + File.separator + "source", "live-stream" + i1 + ".mp4");
        }
      } else {
        f = null;
      }
    } else {
      f = new File(videoDir + File.separator + "source", "live-stream" + count + ".mp4");
    }
    if (f != null && f.exists()) {
      NanoHTTPD.Response res =
          NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "video/mp4",
              new FileInputStream(f), -1);
      res.addHeader("Count", String.valueOf(count));
      return res;
    } else {
      return null;
    }
  }
}
