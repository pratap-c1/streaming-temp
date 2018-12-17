package screen.record.and.serve.models;

public class LatestVideoIdModel {
  public LatestVideoIdModel(Integer latestVideoId) {
    this.latestVideoId = latestVideoId;
  }

  public LatestVideoIdModel() {
  }

  private Integer latestVideoId;

  public Integer getLatestVideoId() {
    return latestVideoId;
  }

  public void setLatestVideoId(Integer latestVideoId) {
    this.latestVideoId = latestVideoId;
  }
}
