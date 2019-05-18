package ${package};

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
public abstract class IntegrationTestBase {

  private static ConfigurableApplicationContext ctx;

  // private ApiClient apiClient = new ApiClient().setBasePath( "http://localhost:11111" ); // set port in application.test.properties
  // private ServerApi server = ServerApi.newInstance(apiClient);


  @BeforeAll
  public static void setupServer() {
    SpringApplication app = null;
    //TODO: Use your Swagger2SpringBoot class here.
    //SpringApplication app = new SpringApplication(Swagger2SpringBoot.class);
    ctx = app.run();
  }

  @AfterAll
  public static void stopServer() {
    if (ctx != null) {
      ctx.close();
    }
  }
}
