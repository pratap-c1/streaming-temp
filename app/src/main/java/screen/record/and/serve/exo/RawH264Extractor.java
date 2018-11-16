package screen.record.and.serve.exo;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.PositionHolder;
import java.io.IOException;

public class RawH264Extractor implements Extractor {
  @Override public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
    return true;
  }

  @Override public void init(ExtractorOutput output) {

  }

  @Override public int read(ExtractorInput input, PositionHolder seekPosition)
      throws IOException, InterruptedException {
    return Extractor.RESULT_CONTINUE;
  }

  @Override public void seek(long position, long timeUs) {

  }

  @Override public void release() {

  }
}
