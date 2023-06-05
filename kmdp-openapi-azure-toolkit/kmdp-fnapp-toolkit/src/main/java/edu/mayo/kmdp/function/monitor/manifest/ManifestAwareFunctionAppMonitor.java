package edu.mayo.kmdp.function.monitor.manifest;

import edu.mayo.kmdp.function.monitor.FunctionAppMonitor;
import edu.mayo.kmdp.function.monitor.MonitorableFunctionApp;
import edu.mayo.kmdp.function.monitor.MonitorableFunctionAppComponent;
import edu.mayo.kmdp.health.MonitorablePropertyDef;
import edu.mayo.kmdp.health.MonitoringSchema;
import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.BuildInfo;
import edu.mayo.kmdp.health.datatype.DeploymentEnvironment;
import edu.mayo.kmdp.health.datatype.Flags;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.StateData;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.Util;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link FunctionAppMonitor}, which uses the {@link MonitorableFunctionApp}
 * as well as the MANIFEST.MF created at build time
 *
 * @see BuildManifest
 */
public class ManifestAwareFunctionAppMonitor implements FunctionAppMonitor {

  protected BuildManifest meta;

  /**
   * Constructor
   *
   * @param klazz The class to be monitored, used to locate the Jar that contains the MANIFEST
   */
  public ManifestAwareFunctionAppMonitor(Class<?> klazz) {
    this.meta = new BuildManifest(klazz);
  }

  protected BuildManifest getMeta() {
    return meta;
  }

  /**
   * Default implementation of /version. Uses the version info from the MANIFEST.MF
   *
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /version
   */
  @Override
  @SuppressWarnings("java:S172")
  public String getVersion(MonitorableFunctionApp target) {
    return getMeta().getVersion();
  }

  /**
   * Default implementation of /state. Uses the MANIFEST.MF metadata, as well as the environment
   * variables reported by the app
   *
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /state
   */
  @Override
  public StateData getState(MonitorableFunctionApp target) {
    var stateData = new StateData();

    stateData.setFeatures(new Flags());

    var buildInfo = new BuildInfo();
    buildInfo.setArtifactId(getMeta().getArtifactId());
    buildInfo.setGroupId(getMeta().getGroupId());
    buildInfo.setVersion(getMeta().getVersion());
    buildInfo.setBuildTime(getMeta().getBuildTime());
    stateData.setBuildConfiguration(buildInfo);

    MiscProperties props = MonitorablePropertyDef.resolveProperties(target.getEnvironmentProperties());
    stateData.setDeploymentEnvironmentConfiguration(props);

    stateData.setSchemaInfo(MonitoringSchema.stateMetaInfo());

    return stateData;
  }

  /**
   * Default implementation of /health. Uses the MANIFEST.MF metadata, as well as the components
   * reported by the app
   *
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /health
   */
  @Override
  public HealthData getHealth(MonitorableFunctionApp target) {
    var healthData = new HealthData();

    healthData.setAt(DateTimeUtil.serializeAsDateTime(new Date()));

    healthData.setSchemaInfo(MonitoringSchema.healthMetaInfo());
    healthData.setVersion(getMeta().getVersion());
    healthData.setName(getMeta().getName());

    List<ApplicationComponent> components = target.getComponents().stream()
        .map(MonitorableFunctionAppComponent::getComponentStatus)
        .collect(Collectors.toList());
    healthData.setComponents(components);
    healthData.setStatus(defaultAggregateStatus(components));
    healthData.setStatusMessage(surfaceStatusMessages(components));

    healthData.setDeploymentEnvironment(getMeta().getVersion().contains("SNAPSHOT")
        ? DeploymentEnvironment.DEV : DeploymentEnvironment.PROD);

    return healthData;
  }

  /**
   * Aggregates the status messages of the component to provide a default, global status
   * <p>
   * If any component is DOWN, limits to the status messages of the components that are DOWN else,
   * if any component is IMPAIRED, limits to the status messages of the IMPAIRED componenets, else
   * returns a generic 'ok'
   *
   * @param components the {@link ApplicationComponent} whose status messages to aggregate
   * @return a default, summary status message
   */
  protected String surfaceStatusMessages(List<ApplicationComponent> components) {
    boolean isDown = components.stream().allMatch(ac -> ac.getStatus() == Status.DOWN);
    if (isDown) {
      return aggregateStatusMessages(components, Status.DOWN);
    }

    boolean isImpaired = components.stream().allMatch(ac -> ac.getStatus() == Status.IMPAIRED);
    if (isImpaired) {
      return aggregateStatusMessages(components, Status.IMPAIRED);
    }

    return "OK";
  }

  /**
   * Aggregates the status messages of the {@link ApplicationComponent} with a given {@link Status}
   * <p>
   * Filters out empty messages, and joins the others
   *
   * @param components the {@link ApplicationComponent} whose status messages to aggregate
   * @param status     a {@link Status} filter
   * @return a combined status message
   */
  protected String aggregateStatusMessages(List<ApplicationComponent> components, Status status) {
    return components.stream()
        .filter(ac -> ac.getStatus() == status)
        .map(ApplicationComponent::getStatusMessage)
        .filter(Util::isNotEmpty)
        .collect(Collectors.joining("|"));
  }

  /**
   * Default logic to determine an app's status based on the status of its components.
   * Conservatively, returns the worst of the statuses
   *
   * @param components the {@link ApplicationComponent} whose statuses to aggregate
   * @return an overall status that is the worst of the input statuses
   */
  protected Status defaultAggregateStatus(List<ApplicationComponent> components) {
    return components.stream()
        .map(ApplicationComponent::getStatus)
        .reduce((s1, s2) -> s1.ordinal() > s2.ordinal() ? s1 : s2)
        .orElse(Status.UP);
  }


}
