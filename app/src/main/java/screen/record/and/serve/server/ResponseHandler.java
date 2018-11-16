package screen.record.and.serve.server;

import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;

public interface ResponseHandler {
  NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session) throws IOException;
}
