package ${package}.client;

import ${package}.ApiClient;
import edu.mayo.kmdp.util.ws.AbstractApiClientFactory;
import edu.mayo.kmdp.util.ws.JsonRestWSUtils.WithFHIR;

public class ApiClientFactory extends AbstractApiClientFactory {

  public ApiClientFactory(String basePath, WithFHIR withFhir) {
    super(basePath,withFhir);
  }

  public ApiClient create() {
    return new ApiClient(this.restTemplate).setBasePath(basePath);
  }

}
