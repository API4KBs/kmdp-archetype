package edu.mayo.kmdp.health.monitor;

import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.StateData;

/**
 * Interface to be implemented by Monitors that can assess the /state, /version and /health of a
 * {@link MonitorableFunctionApp} App, using information provided by the App itself, but also by the
 * common execution environment in which the App is built and deployed.
 * <p>
 * Scope note: the Function App can choose an implementation of Monitor that is compatible with the
 * app itself. The implementation is also supposed to access the execution environment of the app.
 */
public interface FunctionAppMonitor {

  /**
   * No-op implementation of {@link FunctionAppMonitor} that throws
   * {@link UnsupportedOperationException} when invoked
   *
   * @return a {@link FunctionAppMonitor} that throws when invoked
   * @throws UnsupportedOperationException
   */
  static FunctionAppMonitor noMonitor() {
    return new FunctionAppMonitor() {
      @Override
      public String getVersion(MonitorableFunctionApp target) {
        throw new UnsupportedOperationException("/Version not supported");
      }

      @Override
      public StateData getState(MonitorableFunctionApp target) {
        throw new UnsupportedOperationException("/State not supported");
      }

      @Override
      public HealthData getHealth(MonitorableFunctionApp target) {
        throw new UnsupportedOperationException("/Health not supported");
      }
    };
  }

  /**
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /version of the target function
   */
  String getVersion(MonitorableFunctionApp target);

  /**
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /state of the target function
   */
  StateData getState(MonitorableFunctionApp target);

  /**
   * @param target the {@link MonitorableFunctionApp} to assess
   * @return the /health of the target function
   */
  HealthData getHealth(MonitorableFunctionApp target);

}
