package edu.mayo.kmdp.function.util;

import org.slf4j.Logger;

public class FunctionContext {

  private final String functionName;
  private final String invocationId;
  private final Logger logger;

  public FunctionContext(String functionName, String invocationId, Logger logger) {
    this.functionName = functionName;
    this.invocationId = invocationId;
    this.logger = logger;
  }

  public Logger getLogger() {
    return logger;
  }

  public String getFunctionName() {
    return functionName;
  }

  public String getInvocationId() {
    return invocationId;
  }
}
