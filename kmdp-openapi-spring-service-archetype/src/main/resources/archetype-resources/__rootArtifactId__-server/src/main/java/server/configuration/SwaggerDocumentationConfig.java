package ${package}.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerDocumentationConfig {

  @Bean
  public Docket customImplementation() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("${package}"))
        .build()
        //.directModelSubstitute(org.threeten.bp.LocalDate.class, java.sql.Date.class)
        //.directModelSubstitute(org.threeten.bp.OffsetDateTime.class, java.util.Date.class)
        //.apiInfo(apiInfo()
        ;
  }

}
