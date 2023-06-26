package edu.mayo.kmdp.util.ws;

import static edu.mayo.kmdp.function.util.CloudFunctionHelper.fromQuery;
import static edu.mayo.kmdp.function.util.CloudFunctionHelper.getHeader;
import static edu.mayo.kmdp.function.util.CloudFunctionHelper.toResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.cloud.functions.HttpResponse;
import edu.mayo.kmdp.function.util.FunctionContext;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CloudFunctionHelperTest {

  private static final Logger logger = LoggerFactory.getLogger(CloudFunctionHelperTest.class);
  public static final String HEADER_CONTENT_KEY = "content";
  public static final String HEADER_CONTENT_VALUE = "The & should be escaped to {ampersand character}amp;";

  @Test
  void testToResponse() {

    Map<String, List<String>> meta = new HashMap<>();
    meta.put(HEADER_CONTENT_KEY, List.of(HEADER_CONTENT_VALUE));
    Answer<String> answer = Answer.of("200", "{\"status\": \"testing\"}", meta);

    var ctx = new FunctionContext("test", UUID.randomUUID().toString(), logger);

    HttpResponse httpResponse = new TestHttpResponse();
    toResponse(ctx, httpResponse, answer);

    String contentHeaderValue = getHeader(httpResponse, HEADER_CONTENT_KEY)
        .orElseGet(Assertions::fail);
    assertEquals("The &amp; should be escaped to {ampersand character}amp;", contentHeaderValue);

    String explanationHeaderValue = getHeader(httpResponse, "X-Explanation")
        .orElseGet(Assertions::fail);
    assertEquals("", explanationHeaderValue);

    String contentTypeHeaderValue = getHeader(httpResponse, "Content-Type")
        .orElseGet(Assertions::fail);
    assertEquals("application/json", contentTypeHeaderValue);

  }

  @Test
  void testGetQueryParam() {
    String rid = UUID.randomUUID().toString();
    Map<String, List<String>> query = Map.of(
        "myParam2", List.of("value2")
    );

    var v1 = fromQuery(
        "myParam1", null, String.class, false, "defVal", query, rid);
    assertEquals("defVal", v1.get(0));

    var v2 = fromQuery(
        "myParam2", null, String.class, true, null, query, rid);
    assertEquals("value2", v2.get(0));

    assertThrows(ServerSideException.class, () -> fromQuery(
        "myParam3", null, String.class, true, null, query, rid));
  }

  static class TestHttpResponse implements HttpResponse {

    int statusCode;
    private String contentType;

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private final Map<String, List<String>> headers = new HashMap<>();

    @Override
    public void setStatusCode(int code) {
      this.statusCode = code;
    }

    @Override
    public void setStatusCode(int code, String s) {
      setStatusCode(code);
    }

    @Override
    public void setContentType(String s) {
      this.contentType = s;
    }

    @Override
    public Optional<String> getContentType() {
      return Optional.ofNullable(contentType);
    }

    @Override
    public void appendHeader(String s, String s1) {
      headers.computeIfAbsent(s, x -> new ArrayList<>()).add(s1);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
      return headers;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return baos;
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
      return new BufferedWriter(new OutputStreamWriter(baos));
    }
  }


}
