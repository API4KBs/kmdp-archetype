package edu.mayo.kmdp.health.monitor;

import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.StateData;

public class NullMonitor implements FunctionAppMonitor {

  @Override
  public String getVersion(MonitorableFunctionApp target) {
    return "N/A";
  }

  @Override
  public StateData getState(MonitorableFunctionApp target) {
    return new StateData();
  }

  @Override
  public HealthData getHealth(MonitorableFunctionApp target) {
    return new HealthData();
  }
}
