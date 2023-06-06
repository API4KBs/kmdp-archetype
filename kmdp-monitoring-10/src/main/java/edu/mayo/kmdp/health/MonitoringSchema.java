package edu.mayo.kmdp.health;

import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.SchemaMetaInfo;

public class MonitoringSchema {

  public static final String SCHEMA_VERSION = "0.0.1";

  private MonitoringSchema() {
    // functions only
  }

  /**
   * Metadata about the {@link HealthData} schema used to report information
   *
   * @return SchemaMetaInfo for {@link HealthData}
   */
  static SchemaMetaInfo schemaMetaInfo(String subScheme) {
    var info = new SchemaMetaInfo();
    info.setUrl("https://schemas.kmd.mayo.edu/" + subScheme + ".json");
    info.setVersion(SCHEMA_VERSION);
    return info;
  }

  public static SchemaMetaInfo stateMetaInfo() {
     return schemaMetaInfo("state-endpoint");
  }

  public static SchemaMetaInfo healthMetaInfo() {
    return schemaMetaInfo("health-endpoint");
  }

  public static SchemaMetaInfo versionMetaInfo() {
    return schemaMetaInfo("version-endpoint");
  }

}
