package screen.record.and.serve.exo;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class RawH264DataSource extends BaseDataSource {
  private static final String TAG = RawH264DataSource.class.getCanonicalName();
  /**
   * Creates base data source.
   *
   * @param isNetwork Whether the data source loads data through a network.
   */
  protected RawH264DataSource(boolean isNetwork) {
    super(isNetwork);
  }

  private Uri uri;

  public RawH264DataSource() {
    super(true);
  }

  private BufferedReader in;

  @Override public long open(DataSpec dataSpec) throws IOException {
    Log.d(TAG, "C: Connecting...");
    Log.d(TAG, "C: server host = " + dataSpec.uri.getHost());
    Log.d(TAG, "C: server port = " + dataSpec.uri.getPort());
    Socket socket = new Socket("192.168.200.2", 9005);
    Log.d(TAG, "C: is connected to server, " + socket.isConnected());
    this.uri = dataSpec.uri;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    transferStarted(dataSpec);
    return C.LENGTH_UNSET;
  }

  @Override public int read(byte[] buffer, int offset, int readLength) throws IOException {
    String text = new String(buffer, "UTF-8");
    char[] chars = text.toCharArray();
    int bytesRead = in.read(chars, offset, readLength);
    if (bytesRead == -1) {
      return C.RESULT_END_OF_INPUT;
    }
    bytesTransferred(bytesRead);
    return bytesRead;
  }

  @Nullable @Override public Uri getUri() {
    return this.uri;
  }

  @Override public void close() throws IOException {
    if (uri != null) {
      uri = null;
      transferEnded();
    }
    if (in != null) {
      in.close();
      in = null;
    }
  }
}
