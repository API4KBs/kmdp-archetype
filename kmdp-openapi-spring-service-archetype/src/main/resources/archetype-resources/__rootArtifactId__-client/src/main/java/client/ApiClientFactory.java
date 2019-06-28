package ${package}.client;

import edu.mayo.kmdp.util.ws.JsonRestWSUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import edu.mayo.kmdp.util.ws.WebSessionContext;
import edu.mayo.kmdp.util.ws.AuthorizationHeaderForwardConfiguration;
import edu.mayo.kmdp.util.ws.HeaderForwardClientInterceptor;
import ${package}.ApiClient;

public class ApiClientFactory {

  private RestTemplate restTemplate;

  private String basePath;

  public ApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir) {
    this(basePath, withFhir, new HashSet<>(Arrays.asList(AuthorizationHeaderForwardConfiguration.AUTHORIZATION_HEADER)));
  }

  public ApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir, Set<String> forwardHeaders) {
    this.basePath = basePath;
    RestTemplate restTemplate = this.buildDefaultRestTemplate();

    HeaderForwardClientInterceptor interceptor =
            new HeaderForwardClientInterceptor(forwardHeaders);

    restTemplate.getInterceptors().add(interceptor);

    this.restTemplate = JsonRestWSUtils.enableFHIR(restTemplate, withFhir);
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
    RestTemplate restTemplate = new RestTemplate();
    // This allows us to read the response more than once - Necessary for debugging.
    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
    return restTemplate;
  }

}
