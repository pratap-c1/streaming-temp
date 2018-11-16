package screen.record.and.serve.exo;

import com.google.android.exoplayer2.upstream.DataSource;

public class RawH264DataSourceFactory implements DataSource.Factory {
  @Override public DataSource createDataSource() {
    return new RawH264DataSource();
  }
}
