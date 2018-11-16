package screen.record.and.serve.exo;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;

public class RawH264ExtractorFactory implements ExtractorsFactory {
  @Override public Extractor[] createExtractors() {
    return new Extractor[] { new RawH264Extractor() };
  }
}
