package edu.mayo.kmdp.function.monitor;

import edu.mayo.kmdp.health.MonitorablePropertyDef;
import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import java.util.Collections;
import java.util.Set;

/**
 * A Function App Component that is able to provide a health descriptor
 */
public interface MonitorableFunctionAppComponent {

  /**
   * @return the Components's Environment properties, mapped to default values
   * @see MonitorablePropertyDef
   */
  default Set<MonitorablePropertyDef> getComponentPropertiesDefinition() {
    return Collections.emptySet();
  }

  /**
   * @return a /health descriptor of this component
   */
  ApplicationComponent getComponentStatus();

}
