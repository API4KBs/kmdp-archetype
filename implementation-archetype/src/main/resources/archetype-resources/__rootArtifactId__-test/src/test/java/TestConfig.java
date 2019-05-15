package ${package};

;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {"${package}"})
@PropertySource(value={"classpath:application.test.properties"})
public class TestConfig {

  /*
  @Bean
  public {Server} testService() {
  }
  */
}
