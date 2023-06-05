package edu.mayo.kmdp.health;

import static edu.mayo.kmdp.health.utils.MonitorUtil.getServiceNowInfo;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.DeploymentEnvironment;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.SchemaMetaInfo;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.health.utils.MonitorUtil;
import edu.mayo.kmdp.health.utils.PropKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HealthEndPoint implements HealthApiDelegate {

  @Autowired
  protected Environment environment;

  @Autowired
  protected BuildProperties buildProperties;

  /**
   * Application-specific bean to supply component descriptors
   * <p>
   * Uses {@link Supplier} instead of {@link ApplicationComponent} directly to ensure the component
   * descriptors are (re)computed at the time the service is called, to reflect the {@link Status}
   * at that point
   */
  @Autowired(required = false)
  List<Supplier<ApplicationComponent>> appComponentSuppliers;

  /**
   * Application-specific function bean to override the logic for determining the overall
   * application {@link Status}, based on the Status of the {@link ApplicationComponent}
   */
  @Autowired(required = false)
  Function<List<ApplicationComponent>, Status> statusMapper;


  /**
   * Metadata about the {@link HealthData} schema used to report information
   *
   * @return SchemaMetaInfo for {@link HealthData}
   */
  static SchemaMetaInfo schemaMetaInfo() {
    return MonitoringSchema.healthMetaInfo();
  }

  /**
   * Collects {@link HealthData} information for the application as of the point in time this
   * service is invoked
   *
   * @return the HealthData payload
   */
  @Override
  public ResponseEntity<HealthData> getHealthData() {
    var health = new HealthData();

    if(appComponentSuppliers != null) {
      List<ApplicationComponent> comps = appComponentSuppliers.stream()
          .map(Supplier::get)
          .collect(Collectors.toList());

      health.setStatus(detectServerStatus(comps));
      health.setComponents(new ArrayList<>(comps));
    }

    var snInfo = getServiceNowInfo(environment);

    health.setName(snInfo.getDisplay());
    health.setDeploymentEnvironment(getDeploymentEnvironment());
    health.setVersion(buildProperties.getVersion());
    health.setServiceNowReference(snInfo);

    health.setAt(MonitorUtil.formatInstant(Instant.now()));
    health.setSchemaInfo(schemaMetaInfo());

    return ResponseEntity.ok(health);
  }

  /**
   * Infers the {@link Status} of the overall application, given the Status of the application
   * components.
   * <p>
   * If a mapping function is provided by the application, that function will be used. Otherwise,
   * will fall back to a default strategy: the status of the application will be determined by the
   * worst of the component statuses
   *
   * @param comps information about the {@link ApplicationComponent}, which includes the Status of
   *              each component
   * @return the overall application status
   * @see MonitorUtil#defaultAggregateStatus(List)
   */
  private Status detectServerStatus(List<ApplicationComponent> comps) {
    return statusMapper != null
        ? statusMapper.apply(comps)
        : MonitorUtil.defaultAggregateStatus(comps);
  }

  /**
   * Detects the environment tier that the application is deployed on. Looks up the value of a
   * property called "env", otherwise tries to infer the value from the active spring profiles.
   *
   * @return the inferred {@link DeploymentEnvironment}
   */
  protected DeploymentEnvironment getDeploymentEnvironment() {
    return Stream.concat(
            Optional.ofNullable(environment.getProperty(PropKey.ENV.getKey())).stream(),
            Arrays.stream(environment.getActiveProfiles()))
        .flatMap(env -> Arrays.stream(DeploymentEnvironment.values())
            .filter(e -> e.name().equalsIgnoreCase(env))
            .findFirst().stream())
        .findFirst()
        .orElse(DeploymentEnvironment.UNKNOWN);
  }

}
