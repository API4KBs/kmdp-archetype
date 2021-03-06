/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.languages.SpringCodegen;

public class FacadeServerGenerator extends SpringCodegen implements CodegenConfig {


  public static final String DELEGATES_ONLY = "delegatesOnly";
  public static final String CONTROLLERS_ONLY = "controllersOnly";


  public FacadeServerGenerator() {
    super();

    cliOptions.add(CliOption.newBoolean(DELEGATES_ONLY,
        "Assuming server interfaces, delegates and controllers would be generated by "
            + "default, forces that delegates only are actually generated"));

    cliOptions.add(CliOption.newBoolean(CONTROLLERS_ONLY,
        "Assuming server interfaces, delegates and controllers would be generated by "
            + "default, forces that delegates only are actually generated"));
  }

  @Override
  public void processOpts() {
    super.processOpts();

    boolean delegatesOnly = false;
    boolean controllersOnly = false;
    if (additionalProperties.containsKey(DELEGATES_ONLY)) {
      delegatesOnly = (Boolean.valueOf(additionalProperties.get(DELEGATES_ONLY).toString()));
    }
    if (additionalProperties.containsKey(CONTROLLERS_ONLY)) {
      controllersOnly = (Boolean.valueOf(additionalProperties.get(CONTROLLERS_ONLY).toString()));
    }

    if (delegatesOnly && controllersOnly) {
      throw new IllegalArgumentException("Enabling both 'delegatesOnly' and 'controllersOnly' "
          + "results in no code being generated");
    }

    if (delegatesOnly) {
      // no controllers - interfaces only
      apiTemplateFiles.remove("api.mustache");
      apiTemplateFiles.remove("apiController.mustache");
      apiTemplateFiles.put("java-server-internal.mustache", "Internal.java");
    }
    if (controllersOnly) {
      // no interfaces needed
      apiTemplateFiles.put("java-server-internal-adapter.mustache", "InternalAdapter.java");
      apiTemplateFiles.remove("apiDelegate.mustache");
    }

  }


}
