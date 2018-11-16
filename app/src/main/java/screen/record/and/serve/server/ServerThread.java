package screen.record.and.serve.server;

import android.os.Handler;
import android.util.Log;
import com.blankj.utilcode.util.NetworkUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class ServerThread implements Runnable {
  private final Logger logger = Logger.getLogger(ServerThread.class.getName());
  private static final String TAG = ServerThread.class.getCanonicalName();
  // DEFAULT IP
  public static String SERVER_IP = "192.168.200.2";

  // DESIGNATE A PORT
  public static final int SERVER_PORT = 9005;

  private Handler handler;

  private ServerSocket serverSocket;

  private AtomicBoolean isClosed = new AtomicBoolean(false);

  private Recorder mRecorder;

  public ServerThread(Handler handler, Recorder recorder) {
    this.handler = handler;
    this.mRecorder = recorder;
    SERVER_IP = NetworkUtils.getIPAddress(true);
    Thread fst = new Thread(this);
    fst.start();
  }

  @Override public void run() {
    try {
      if (SERVER_IP != null) {
        handler.post(() -> {
          logger.log(Level.FINE, "Listening on IP: " + SERVER_IP);
          //serverStatus.setText("Listening on IP: " + SERVER_IP);

        });
        serverSocket = new ServerSocket(SERVER_PORT);
        while (!isClosed.get()) {
          // LISTEN FOR INCOMING CLIENTS
          Log.d(TAG, "calling accept");
          Socket client = serverSocket.accept();
          Log.d(TAG, format("accepted connection client = %s", client.toString()));
          handler.post(() -> {
            logger.log(Level.FINE, "Connected.");
            Log.d(TAG, "Connected");
            //serverStatus.setText("Connected.");
          });

          try {
            handler.post(() -> {
              Log.d(TAG, "starting recording");
              mRecorder.startRecord(client);
              Log.d(TAG, "starting recording");
              // DO WHATEVER YOU WANT TO THE FRONT END
              // THIS IS WHERE YOU CAN BE CREATIVE
            });
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            Log.d(TAG, format("BufferedReader = %s", in.toString()));
            String line = null;
            while ((line = in.readLine()) != null) {
              Log.d(TAG, "in while loop");
              Log.d("ServerActivity", line);
            }
            break;
          } catch (Exception e) {
            handler.post(() -> {
              logger.log(Level.FINE, "Oops. Connection interrupted. Please reconnect your phones.");
              //serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
            });
            e.printStackTrace();
          }
        }
      } else {
        handler.post(() -> {
          logger.log(Level.FINE, "Couldn't detect internet connection.");
          //serverStatus.setText("Couldn't detect internet connection.");
        });
      }
    } catch (Exception e) {
      handler.post(() -> {
        logger.log(Level.FINE, "Error");
        //serverStatus.setText("Error");
      });
      e.printStackTrace();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      mRecorder = null;
      isClosed.set(true);
    }
  }
}
