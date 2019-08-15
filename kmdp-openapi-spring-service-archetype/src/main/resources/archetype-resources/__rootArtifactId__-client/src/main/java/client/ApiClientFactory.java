package ${package}.client;

import edu.mayo.kmdp.util.ws.AuthorizationHeaderForwardConfiguration;
import edu.mayo.kmdp.util.ws.HeaderForwardClientInterceptor;
import edu.mayo.kmdp.util.ws.JsonRestWSUtils;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.omg.spec.api4kp._1_0.ServerSideException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ${package}.ApiClient;

public class ApiClientFactory {

  private RestTemplate restTemplate;

  private String basePath;

  public ApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir) {
    this(basePath, withFhir, new HashSet<>(Arrays.asList(AuthorizationHeaderForwardConfiguration.AUTHORIZATION_HEADER)));
  }

  public ApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir, Set<String> forwardHeaders) {
    this.basePath = basePath;
    RestTemplate template = this.buildDefaultRestTemplate();

    HeaderForwardClientInterceptor interceptor =
            new HeaderForwardClientInterceptor(forwardHeaders);

    template.getInterceptors().add(interceptor);

    this.restTemplate = JsonRestWSUtils.enableFHIR(template, withFhir);
  }

  public ApiClientFactory(String basePath, RestTemplate restTemplate) {
    this.basePath = basePath;
    this.restTemplate = restTemplate;
  }

  public ApiClient create() {
    ApiClient apiClient = new ApiClient(this.restTemplate);
    apiClient.setBasePath(this.basePath);
    return apiClient;
  }

  protected RestTemplate buildDefaultRestTemplate() {
    RestTemplate template = new RestTemplate();
    // This allows us to read the response more than once - Necessary for debugging.
    template.setRequestFactory(new BufferingClientHttpRequestFactory(template.getRequestFactory()));
    template.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      protected void handleError(ClientHttpResponse response, HttpStatus statusCode)
          throws IOException {
        throw new ServerSideException(
            ResponseCode.resolve(Integer.toString(response.getRawStatusCode())).orElse(ResponseCode.InternalServerError),
            response.getHeaders(),
            getResponseBody(response));
      }
    });
    return template;
  }

}
