package screen.record.and.serve.view;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import screen.record.and.serve.R;
import screen.record.and.serve.databinding.ActivityExoBinding;
import screen.record.and.serve.exo.RawH264DataSourceFactory;
import screen.record.and.serve.exo.RawH264ExtractorFactory;

public class ExoActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityExoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_exo);
    //initiate Player
    //Create a default TrackSelector
    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    //Create the player
    SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    binding.simplePlayer.setPlayer(player);

    //RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
    //// This is the MediaSource representing the media to be played.
    //MediaSource videoSource =
    //    new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(
    //        Uri.parse("rtsp://192.168.200.2:8086"));
    RawH264DataSourceFactory rawH264DataSourceFactory = new RawH264DataSourceFactory();
    ExtractorMediaSource.Factory extractorMediaSourceFactory =
        new ExtractorMediaSource.Factory(rawH264DataSourceFactory);
    extractorMediaSourceFactory.setExtractorsFactory(new RawH264ExtractorFactory());
    MediaSource videoSource =
        extractorMediaSourceFactory.createMediaSource(Uri.parse("192.168.200.2:9005"));

    player.prepare(videoSource);
    //auto start playing
    player.setPlayWhenReady(true);
  }
}
