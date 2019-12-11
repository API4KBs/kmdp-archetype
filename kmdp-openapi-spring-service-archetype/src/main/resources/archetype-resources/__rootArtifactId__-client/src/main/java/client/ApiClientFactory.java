package ${package}.client;

import ${package}.ApiClient;
import edu.mayo.kmdp.util.ws.AbstractApiClientFactory;
import edu.mayo.kmdp.util.ws.JsonRestWSUtils.WithFHIR;

public class ApiClientFactory extends AbstractApiClientFactory {

  public ApiClientFactory(String basePath, WithFHIR withFhir) {
    super(basePath,withFhir);
  }

  public ApiClientFactory(String basePath) {
    super(basePath,WithFHIR.STU3);
  }

  public ApiClient create() {
    return new ApiClient(this.restTemplate).setBasePath(basePath);
  }

}
