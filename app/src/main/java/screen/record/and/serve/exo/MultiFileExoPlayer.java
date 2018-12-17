package screen.record.and.serve.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import java.io.File;

import static com.google.android.exoplayer2.Player.STATE_ENDED;

public class MultiFileExoPlayer {
  private static final String TAG = MultiFileExoPlayer.class.getCanonicalName();
  private static File videoDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

  public static boolean multiFilePlay(Context context, PlayerView playerView, int count) {
    File f = new File(videoDir + File.separator + "source", "my.mp4");
    if (!f.exists()) {
      Log.d(TAG, "no file");
      Toast.makeText(context, "No file", Toast.LENGTH_SHORT).show();
      return false;
    }
    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    //Create the player
    SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
    playerView.setPlayer(player);
    player.addListener(new Player.EventListener() {
      @Override
      public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

      }

      @Override public void onTracksChanged(TrackGroupArray trackGroups,
          TrackSelectionArray trackSelections) {

      }

      @Override public void onLoadingChanged(boolean isLoading) {

      }

      @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (STATE_ENDED == playbackState) {
          Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + ", " + playbackState);
          player.release();
          multiFilePlay(context, playerView, count);
        }
      }

      @Override public void onRepeatModeChanged(int repeatMode) {

      }

      @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

      }

      @Override public void onPlayerError(ExoPlaybackException error) {
        Log.d(TAG, "onPlayerError: " + error);
        player.release();
        multiFilePlay(context, playerView, count);
      }

      @Override public void onPositionDiscontinuity(int reason) {

      }

      @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

      }

      @Override public void onSeekProcessed() {

      }
    });

    DefaultDataSourceFactory defaultDataSourceFactory =
        new DefaultDataSourceFactory(context, "android");
    ExtractorMediaSource.Factory extractorMediaSourceFactory =
        new ExtractorMediaSource.Factory(defaultDataSourceFactory);
    MediaSource videoSource = extractorMediaSourceFactory.createMediaSource(Uri.fromFile(f));

    player.prepare(videoSource);
    //auto start playing
    player.setPlayWhenReady(true);
    return true;
  }

  public static boolean multiUrlPlay(String authority, Integer port, Context context,
      PlayerView playerView, int count) {
    return multiUrlPlay(authority, port, context, playerView, count, null);
  }

  public static boolean multiUrlPlay(String authority, Integer port, Context context,
      PlayerView playerView, int count, @Nullable TextView infoTv) {
    Uri uri = new Uri.Builder().scheme("http")
        .encodedAuthority(authority + ":" + (port == null ? 80 : port))
        .path("live-stream3")
        .appendPath(String.valueOf(count))
        .build();

    if (uri == null) {
      Log.d(TAG, "no file");
      return false;
    }
    Log.d(TAG, uri.toString());
    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    //Create the player
    SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
    playerView.setPlayer(player);
    player.addListener(new Player.EventListener() {
      @Override
      public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

      }

      @Override public void onTracksChanged(TrackGroupArray trackGroups,
          TrackSelectionArray trackSelections) {

      }

      @Override public void onLoadingChanged(boolean isLoading) {

      }

      @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (STATE_ENDED == playbackState) {
          Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + ", " + playbackState);
          player.release();
          multiUrlPlay(authority, port, context, playerView, count, infoTv);
        }
      }

      @Override public void onRepeatModeChanged(int repeatMode) {

      }

      @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

      }

      @Override public void onPlayerError(ExoPlaybackException error) {
        Log.d(TAG, "onPlayerError: " + error);
        player.release();
        multiUrlPlay(authority, port, context, playerView, count, infoTv);
      }

      @Override public void onPositionDiscontinuity(int reason) {

      }

      @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

      }

      @Override public void onSeekProcessed() {

      }
    });

    DefaultDataSourceFactory defaultDataSourceFactory =
        new DefaultDataSourceFactory(context, "android");
    ExtractorMediaSource.Factory extractorMediaSourceFactory =
        new ExtractorMediaSource.Factory(defaultDataSourceFactory);
    MediaSource videoSource = extractorMediaSourceFactory.createMediaSource(uri);

    player.prepare(videoSource);
    //auto start playing
    player.setPlayWhenReady(true);
    if (infoTv != null) {
      infoTv.setText(uri.toString());
    }
    return true;
  }
}
