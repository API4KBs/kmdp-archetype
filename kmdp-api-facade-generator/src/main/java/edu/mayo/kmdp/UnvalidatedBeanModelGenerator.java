package edu.mayo.kmdp;

import io.swagger.codegen.languages.SpringCodegen;

public class UnvalidatedBeanModelGenerator extends SpringCodegen {

  @Override
  public void processOpts() {
    super.processOpts();

    /// fix for bug https://github.com/swagger-api/swagger-codegen/issues/8000
    if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
      writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
    }
  }
}
