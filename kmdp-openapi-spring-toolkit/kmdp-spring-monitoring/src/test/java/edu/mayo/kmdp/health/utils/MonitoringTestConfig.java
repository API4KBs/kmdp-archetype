package edu.mayo.kmdp.health.utils;

import edu.mayo.kmdp.health.HealthEndPoint;
import edu.mayo.kmdp.health.StateEndPoint;
import edu.mayo.kmdp.health.VersionEndPoint;
import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.util.PropertiesUtil;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses =
    {StateEndPoint.class, HealthEndPoint.class, VersionEndPoint.class})
public class MonitoringTestConfig {

  @Bean
  BuildProperties mockBuildInfo() {
    Properties props = PropertiesUtil.props()
        .set("name", "Mock")
        .set("artifact", "mockLib")
        .set("group", "mockGrp")
        .set("version", "0.0.0-FAKE")
        .get();
    return new BuildProperties(props);
  }

  @Bean
  Supplier<ApplicationComponent> fakeComponent1() {
    return () -> {
      MiscProperties props = new MiscProperties();
      props.put("attr", "val");

      ApplicationComponent comp = new ApplicationComponent();
      comp.setName("Mock Piece");
      comp.setStatus(Status.IMPAIRED);
      comp.setDetails(props);
      return comp;
    };
  }

  @Bean
  @Qualifier("flag")
  Predicate<String> isFlag() {
    return key -> key != null && key.contains("flag");
  }

  @Bean
  @Qualifier("secret")
  Predicate<String> isSecret() {
    return key -> MonitorUtil.defaultIsSecret(key) || "my.override".equals(key);
  }
}
