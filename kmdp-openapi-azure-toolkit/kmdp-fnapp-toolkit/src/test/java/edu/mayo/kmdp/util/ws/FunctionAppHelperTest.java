package edu.mayo.kmdp.util.ws;

import static edu.mayo.kmdp.function.util.FunctionAppHelper.fromQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpResponseMessage.Builder;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import edu.mayo.kmdp.function.util.FunctionAppHelper;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;

class FunctionAppHelperTest {

  public static final String HEADER_CONTENT_KEY = "content";
  public static final String HEADER_CONTENT_VALUE = "The & should be escaped to {ampersand character}amp;";

  @Test
  void testToResponse() {

    HttpRequestMessage<Optional<String>> request = new TestHttpRequestMessage<>();

    Map<String, List<String>> meta = new HashMap<>();
    meta.put(HEADER_CONTENT_KEY, List.of(HEADER_CONTENT_VALUE));
    Answer<String> answer = Answer.of("200", "{\"status\": \"testing\"}", meta);

    HttpResponseMessage httpResponseMessage = FunctionAppHelper.toResponse(request, answer);

    String contentHeaderValue = httpResponseMessage.getHeader(HEADER_CONTENT_KEY);
    assertEquals("The &amp; should be escaped to {ampersand character}amp;", contentHeaderValue);

    String explanationHeaderValue = httpResponseMessage.getHeader("X-Explanation");
    assertEquals("", explanationHeaderValue);

    String contentTypeHeaderValue = httpResponseMessage.getHeader("Content-Type");
    assertEquals("application/json", contentTypeHeaderValue);

  }

  @Test
  void testGetQueryParam() {
    String rid = UUID.randomUUID().toString();
    Map<String,String> query = Map.of(
        "myParam2", "value2"
    );

    String v1 = fromQuery(
        "myParam1", null, String.class, false, "defVal", query, rid);
    assertEquals("defVal", v1);

    String v2 = fromQuery(
        "myParam2", null, String.class, true, null, query, rid);
    assertEquals("value2", v2);

    assertThrows(ServerSideException.class, () -> fromQuery(
        "myParam3", null, String.class, true, null, query, rid));
  }

  static class TestHttpRequestMessage<T> implements HttpRequestMessage<T> {

    @Override
    public URI getUri() {
      return null;
    }

    @Override
    public HttpMethod getHttpMethod() {
      return HttpMethod.GET;
    }

    @Override
    public Map<String, String> getHeaders() {
      return null;
    }

    @Override
    public Map<String, String> getQueryParameters() {
      return null;
    }

    @Override
    public T getBody() {
      return null;
    }

    @Override
    public Builder createResponseBuilder(HttpStatus httpStatus) {
      return new TestBuilder(httpStatus);
    }

    @Override
    public Builder createResponseBuilder(HttpStatusType httpStatusType) {
      return new TestBuilder().status(httpStatusType);
    }

  }


  static class TestHttpResponseMessage implements HttpResponseMessage {

    protected Object body;
    protected HttpStatusType httpStatusType;
    protected Map<String, String> headers;

    public TestHttpResponseMessage(HttpStatusType httpStatusType, Object body, Map<String, String> headers) {
      this.body = body;
      this.headers = headers;
      this.httpStatusType = httpStatusType;
    }

    @Override
    public HttpStatusType getStatus() {
      return httpStatusType;
    }

    @Override
    public String getHeader(String key) {
      return headers.get(key);
    }

    @Override
    public Object getBody() {
      return body;
    }

  }


  static class TestBuilder implements Builder {

    protected Object body;
    protected HttpStatus httpStatus;
    protected HttpStatusType httpStatusType;
    protected Map<String, String> headers = new HashMap<>();

    public TestBuilder() {
    }

    public TestBuilder(HttpStatus httpStatus) {
      this.httpStatus = httpStatus;
    }

    @Override
    public Builder status(HttpStatusType httpStatusType) {
      this.httpStatusType = httpStatusType;
      return this;
    }

    @Override
    public Builder header(String key, String value) {
      headers.put(key, value);
      return this;
    }

    @Override
    public Builder body(Object body) {
      this.body = body;
      return this;
    }

    @Override
    public HttpResponseMessage build() {
      return new TestHttpResponseMessage(httpStatusType, body, headers);
    }

  }

}
