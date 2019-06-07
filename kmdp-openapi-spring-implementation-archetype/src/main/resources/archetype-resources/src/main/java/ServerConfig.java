package ${package};

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@Profile({"default"})
@PropertySource(value={"classpath:application.properties"})
public class ServerConfig {


}
