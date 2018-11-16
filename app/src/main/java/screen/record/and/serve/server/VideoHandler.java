package screen.record.and.serve.server;

import android.webkit.MimeTypeMap;
import com.blankj.utilcode.util.FileUtils;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VideoHandler implements ResponseHandler {
  private File file;

  VideoHandler(File file) {
    this.file = file;
  }

  @Override public NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session)
      throws IOException {
    NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(com.blankj.utilcode.util.FileUtils.getFileExtension(file)),
        new FileInputStream(file), -1);
    response.addHeader("Content-Disposition",
        "inline; filename=\"" + FileUtils.getFileName(file) + "\"");
    return response;
  }
}
