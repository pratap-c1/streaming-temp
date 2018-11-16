package screen.record.and.serve.models;

public class BaseResponseModel<T> {
  private T result;
  private Integer code;
  private String message;
  private String name;

  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static BaseResponseModel new500Error(Throwable t) {
    return new500Error(t.getMessage());
  }

  public static BaseResponseModel new500Error(String error) {
    BaseResponseModel<String> response = new BaseResponseModel<>();
    response.setCode(500);
    response.setMessage("InternalServerError");
    response.setMessage(error);
    return response;
  }

  public static BaseResponseModel new404Error(String notFoundMessage) {
    BaseResponseModel<String> response = new BaseResponseModel<>();
    response.setCode(404);
    response.setMessage("NotFound");
    response.setMessage(notFoundMessage);
    return response;
  }
}
