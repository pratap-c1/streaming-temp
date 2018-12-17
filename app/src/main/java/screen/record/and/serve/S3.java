package screen.record.and.serve;

import java.io.InputStream;
import java.nio.file.Path;

public interface S3 {
  String uploadFile(String bucketName, String fileObjKeyName, Path filePath);

  InputStream downloadFile(Path filePath, String bucketName, String fileObjKeyName);

  String signedUrl(String bucketName, String objectKey);
}
