package screen.record.and.serve.exo;

import com.google.android.exoplayer2.upstream.DataSource;
import java.io.InputStream;

public class RawH264DataSourceFactory implements DataSource.Factory {
  private InputStream is;

  public RawH264DataSourceFactory() {
  }

  public RawH264DataSourceFactory(InputStream is) {
    this.is = is;
  }

  @Override public DataSource createDataSource() {
    if (is == null) {
      return new RawH264DataSource();
    } else {
      return new RawH264DataSource(is);
    }
  }
}
