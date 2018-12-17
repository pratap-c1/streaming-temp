package screen.record.and.serve.view;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.google.android.exoplayer2.Player;
import java.io.File;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityExoBinding;
import screen.record.and.serve.exo.MultiFileExoPlayer;
import screen.record.and.serve.models.Data;
import screen.record.and.serve.retrofit.DownloadVideoImpl;
import screen.record.and.serve.retrofit.LatestVideoIdServiceImpl;

public class ExoActivity extends AppCompatActivity {
  private ActivityExoBinding binding;
  private Data data;
  private File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    data = new Data();
    binding = DataBindingUtil.setContentView(this, R.layout.activity_exo);
    binding.setActivity(this);
    binding.setData(data);
    // create dirs
    File f1 = new File(videoDir, "source");
    if (!f1.exists()) {
      f1.mkdirs();
    }
    f1 = new File(videoDir, "hls");
    if (!f1.exists()) {
      f1.mkdirs();
    }
  }

  public void multiPlayBack() {
    Uri uri = new Uri.Builder().scheme("http")
        .encodedAuthority(data.ipAddress + ":" + (data.port == null ? 80 : data.port))
        .build();
    new LatestVideoIdServiceImpl().callAPI(uri.toString(), (success, lvm) -> {
      if (success) {
        Toast.makeText(ExoActivity.this, "Latest video ID: " + lvm.getLatestVideoId(),
            Toast.LENGTH_SHORT).show();
        MultiFileExoPlayer.multiUrlPlay(data.ipAddress,
            data.port == null ? 80 : Integer.parseInt(data.port), getApplicationContext(),
            binding.simplePlayer, lvm.getLatestVideoId(), binding.info);
      } else {
        Toast.makeText(ExoActivity.this, "No Video", Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void playSingleDownloadedFile() {
    Uri uri = new Uri.Builder().scheme("http")
        .encodedAuthority(data.ipAddress + ":" + (data.port == null ? 80 : data.port))
        .build();
    new DownloadVideoImpl().callAPI(uri.toString(), 1, () -> runOnUiThread(
        () -> MultiFileExoPlayer.multiFilePlay(getApplicationContext(), binding.simplePlayer, 1)));
  }

  public void playBackStop() {
    Player player = binding.simplePlayer.getPlayer();
    if (player != null) {
      player.stop();
      player.release();
    }
    binding.simplePlayer.setPlayer(null);
  }
}
