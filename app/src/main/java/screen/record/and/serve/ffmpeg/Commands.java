package screen.record.and.serve.ffmpeg;

import java.io.File;

public class Commands {
  public static String[] breakToHls(File sourceFile, File destDir, String m3u8FileName) {
    String[] cmds = new String[35];
    cmds[0] = "-i";
    cmds[1] = sourceFile.getAbsolutePath();
    cmds[2] = "-preset";
    cmds[3] = "fast";
    cmds[4] = "-c:v";
    cmds[5] = "libx264";
    cmds[6] = "c:a";
    cmds[7] = "acc";
    cmds[8] = "-ac";
    cmds[9] = "1";
    cmds[10] = "-strict";
    cmds[11] = "-2";
    cmds[12] = "-crf";
    cmds[13] = "18";
    cmds[14] = "-profile:v";
    cmds[15] = "baseline";
    cmds[16] = "-maxrate";
    cmds[17] = "1000k";
    cmds[18] = "bufsize";
    cmds[19] = "1835k";
    cmds[20] = "=pix_fmt";
    cmds[21] = "yuv420p";
    cmds[22] = "-b:a";
    cmds[23] = "64k";
    cmds[24] = "-flags";
    cmds[25] = "-global_header";
    cmds[26] = "-hls_time";
    cmds[27] = "20";
    cmds[28] = "-hls_list_size";
    cmds[29] = "6";
    cmds[30] = "-hls_wrap";
    cmds[31] = "10";
    cmds[32] = "-start_number";
    cmds[33] = "1";
    cmds[34] = destDir.getAbsolutePath() + File.separator + m3u8FileName + ".m3u8";

    //String command =
    //    "ffmpeg -i {{INPUT_FILE}} -preset fast -c:v libx264 -c:a aac -ac 1 -strict -2 -crf 18 -profile:v baseline -maxrate 1000k -bufsize 1835k "
    //        + "-pix_fmt yuv420p -b:a 64k -flags -global_header -hls_time 20 -hls_list_size 6 -hls_wrap 10 -start_number 1 {{DEST_FILE}}";
    //command = command.replace("{{INPUT_FILE}}", sourceFile.getAbsolutePath());
    //command = command.replace("{{DEST_FILE}}",
    //    destDir.getAbsolutePath() + File.separator + m3u8FileName + ".m3u8");
    // return new String[] {command};
    return cmds;
  }

  public static String[] breakToRtsp(File sourceFile) {
    String a = "ffmpeg - i input - f rtsp - rtsp_transport tcp rtsp://localhost:8888/live.sdp";
    String[] cmds = new String[7];
    cmds[0] = "-i";
    cmds[1] = sourceFile.getAbsolutePath();
    cmds[2] = "-f";
    cmds[3] = "rtsp";
    cmds[4] = "rtsp_transport";
    cmds[5] = "tcp";
    cmds[6] = "rtsp://localhost:8888/live.sdp";
    return cmds;
  }
}
