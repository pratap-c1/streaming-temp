package screen.record.and.serve.server;

import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestHandler implements ResponseHandler {
  @Override public NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session)
      throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put("hello", "world");
    return AndroidWebServer.jsonResponse(map);
  }
}
