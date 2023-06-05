package edu.mayo.kmdp.function.monitor;

import edu.mayo.kmdp.health.MonitorablePropertyDef;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.StateData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.omg.spec.api4kp._20200801.Answer;

/**
 * Interface to be implemented by a Function App that implements the /state, /health and /version
 * endpoints. Delegates the evaluation to a {@link FunctionAppMonitor} object provided by the
 * Function itself
 */
public interface MonitorableFunctionApp {

  /**
   * The /version endpoint
   *
   * @return /version, wrapped
   */
  default Answer<String> getVersion() {
    return Answer.of(getMonitor().getVersion(this));
  }

  /**
   * The /state endpoint
   *
   * @return /state, wrapped
   */
  default Answer<StateData> getState() {
    return Answer.of(getMonitor().getState(this));
  }

  /**
   * The /health endpoint
   *
   * @return /health, wrapped
   */
  default Answer<HealthData> getHealth() {
    return Answer.of(getMonitor().getHealth(this));
  }

  /**
   * @return the Monitor used to assess the function state, and build the response payloads
   */
  FunctionAppMonitor getMonitor();


  static MonitorableFunctionApp unMonitorable() {
    return new MonitorableFunctionApp() {
      @Override
      public FunctionAppMonitor getMonitor() {
        throw new UnsupportedOperationException("This function does not support monitoring");
      }
    };
  }

  /**
   * @return the Function's {@link MonitorableFunctionApp} components, if any.
   */
  default List<MonitorableFunctionAppComponent> getComponents() {
    return Collections.emptyList();
  }

  /**
   * @return the Function's Environment properties, mapped to Optional default values
   * <p>
   * @see MonitorablePropertyDef
   */
  default Set<MonitorablePropertyDef> getEnvironmentProperties() {
    return Collections.emptySet();
  }

}
