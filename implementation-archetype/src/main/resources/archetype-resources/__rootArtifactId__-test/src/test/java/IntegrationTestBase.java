package $

{package};

import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {/* TODO -- add your Spring config */})
public abstract class IntegrationTestBase {

  private ConfigurableApplicationContext ctx;

  @org.junit.Before
  public void setupServer() {
    SpringApplication app = null;
    //TODO: Use your Swagger2SpringBoot class here.
    //SpringApplication app = new SpringApplication(Swagger2SpringBoot.class);
    this.ctx = app.run();
  }

  @org.junit.After
  public void stopServer() {
    ctx.close();
  }

}
