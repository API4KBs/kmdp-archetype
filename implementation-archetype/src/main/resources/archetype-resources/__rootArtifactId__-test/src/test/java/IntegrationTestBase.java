package ${package};

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


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
