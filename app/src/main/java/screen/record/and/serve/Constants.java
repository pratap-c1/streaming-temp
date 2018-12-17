package screen.record.and.serve;

import android.os.Environment;
import java.io.File;

public interface Constants {
  String SOURCE_DIR_NAME = "source";
  String HLS_DIR_NAME = "hls";
  File MAIN_VIDEO_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
  File HLS_DIR = new File(MAIN_VIDEO_DIR.getAbsolutePath(), HLS_DIR_NAME);
  File SOURCE_DIR = new File(MAIN_VIDEO_DIR.getAbsolutePath(), SOURCE_DIR_NAME);
  File SINGLE_VIDEO_FILE =
      new File(MAIN_VIDEO_DIR.getAbsolutePath(), SOURCE_DIR_NAME + File.separator + "my.mp4");
}
