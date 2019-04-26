package {package};

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {/* TODO -- add your Spring config */})
public abstract class IntegrationTestBase {

  private ConfigurableApplicationContext ctx;

  @BeforeAll
  public void setupServer() {
    SpringApplication app = null;
    //TODO: Use your Swagger2SpringBoot class here.
    //SpringApplication app = new SpringApplication(Swagger2SpringBoot.class);
    this.ctx = app.run();
  }

  @AfterAll
  public void stopServer() {
    if (ctx != null) {
      ctx.close();
    }
  }
}
