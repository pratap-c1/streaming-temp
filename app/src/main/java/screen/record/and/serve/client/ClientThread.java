package screen.record.and.serve.client;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOUtils;
import screen.record.and.serve.server.ServerThread;

public class ClientThread implements Runnable {
  private String serverIpAddress;
  private AtomicBoolean connected;
  private Context context;
  private final String TAG = ClientThread.class.getCanonicalName();
  private final File file;
  private final VideoView videoView;

  public void setConnected(boolean connected) {
    Log.d(TAG, "setting connected to false");
    this.connected.set(connected);
  }

  public ClientThread(String serverIpAddress, boolean connected, Context context, File file,
      VideoView videoView) {
    this.serverIpAddress = serverIpAddress;
    this.connected = new AtomicBoolean(connected);
    this.context = context.getApplicationContext();
    this.file = file;
    this.videoView = videoView;
  }

  @Override public void run() {
    try {
      InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
      Log.d("ClientActivity", "C: Connecting...");
      Socket socket = new Socket(serverAddr, ServerThread.SERVER_PORT);
      while (connected.get()) {
        try {
          Log.d("ClientActivity", "C: Sending command.");
          //PrintWriter out =
          //    new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
          //        true);
          // WHERE YOU ISSUE THE COMMANDS
          IOUtils.copy(socket.getInputStream(), new FileOutputStream(file));
          videoView.setVideoURI(Uri.fromFile(file));
          //out.println("Hey Server!");
          Log.d("ClientActivity", "C: Sent.");
        } catch (Exception e) {
          Log.e("ClientActivity", "S: Error", e);
        }
      }
      socket.close();
      Log.d("ClientActivity", "C: Closed.");
    } catch (Exception e) {
      Log.e("ClientActivity", "C: Error", e);
      connected.set(false);
    }
  }
}
