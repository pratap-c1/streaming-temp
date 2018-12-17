package screen.record.and.serve.interfaces;

import java.io.IOException;

public interface RecordingInterface {
  void stopRecording();

  void project() throws IOException;
}
