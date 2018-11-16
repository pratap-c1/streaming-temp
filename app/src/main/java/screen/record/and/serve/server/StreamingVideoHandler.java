package screen.record.and.serve.server;

import android.util.Log;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.String.format;

class StreamingVideoHandler implements ResponseHandler, Runnable {
  private InputStream streamingVideoInputStream;
  private PipedInputStream sink;
  private PipedOutputStream source;
  private String serverIpAddress;
  private Socket socket;

  StreamingVideoHandler(String serverIpAddress) throws IOException {
    this.serverIpAddress = serverIpAddress;
  }

  @Override public NanoHTTPD.Response handleResponse(NanoHTTPD.IHTTPSession session)
      throws IOException {
    InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
    socket = new Socket(serverAddr, ServerThread.SERVER_PORT);
    this.streamingVideoInputStream = socket.getInputStream();
    this.sink = new PipedInputStream();
    this.source = new PipedOutputStream(sink);
    Log.d(TAG, format("is connected : %s", socket.isConnected()));
    Thread t = new Thread(this);
    t.start();
    try {
      Thread.sleep(2000L);// just to let the PipedOutputStream start up
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Return the PipedInputStream to the requestor.
    // Important to have the filesize argument
    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "video/mp4", sink, -1);
  }

  private static final String TAG = StreamingVideoHandler.class.getCanonicalName();

  @Override public void run() {
    Log.d(TAG, "run");
    Log.d(TAG, format("is connected : %s", socket.isConnected()));
    try {
      byte[] b = new byte[1024];
      int len;
      while ((len = streamingVideoInputStream.read(b, 0, 1024)) != -1) {
        Log.d(TAG, format("in while loop, len = %d", len));
        source.write(b, 0, len);
        source.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
