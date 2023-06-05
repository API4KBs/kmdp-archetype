/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.java.AbstractJavaCodegen;
import java.util.Arrays;
import java.util.Optional;

public class FnappHttpServerGenerator extends AbstractJavaCodegen implements CodegenConfig {

  public static final String FNAPP_J = "JavaFnApp";
  public static final String CONTROLLERS_ONLY = "controllersOnly";
  private static final String SELECTED_OPERATIONS = "selectedOperations";
  private static final String CONTEXT_AWARE = "invocationContextAware";

  public FnappHttpServerGenerator() {
    super();

    cliOptions.add(
        CliOption.newString(SELECTED_OPERATIONS,
            "Comma-separated list of operation IDs"));
    cliOptions.add(
        CliOption.newString(CONTEXT_AWARE,
            "If true, expect to propgate request ID"));

    cliOptions.add(CliOption.newBoolean(CONTROLLERS_ONLY,
        "If true, will only generate the FunctionApp request handler"));

    embeddedTemplateDir = templateDir = FNAPP_J;
  }

  @Override
  public String getDefaultTemplateDir() {
    return FNAPP_J;
  }

  @Override
  public CodegenType getTag() {
    return CodegenType.SERVER;
  }

  @Override
  public String getName() {
    return "java-fnapp";
  }

  @Override
  public String getHelp() {
    return "TODO";
  }

  @Override
  public void processOpts() {
    super.processOpts();

    boolean controllersOnly = false;
    if (additionalProperties.containsKey(CONTROLLERS_ONLY)) {
      controllersOnly = (Boolean.valueOf(additionalProperties.get(CONTROLLERS_ONLY).toString()));
    }

    supportsInheritance = true;

    modelTemplateFiles.clear();
    apiTestTemplateFiles.clear();
    modelDocTemplateFiles.clear();
    apiDocTemplateFiles.clear();
    supportingFiles.clear();
    apiTemplateFiles.clear();

    apiTemplateFiles.put("java-fnapp-http.mustache", "Function.java");

    if (!controllersOnly) {
      apiTemplateFiles.put("java-server-internal.mustache", "Internal.java");
    }

    supportedLibraries.put(FNAPP_J, "Java Azure Function App Shell");
    setLibrary(FNAPP_J);

  }

  @Override
  public void addHandlebarHelpers(Handlebars handlebars) {
    super.addHandlebarHelpers(handlebars);
    handlebars.registerHelper("normalizePath", normalizePath());
    Optional.ofNullable(additionalProperties.get(SELECTED_OPERATIONS))
        .ifPresent(select ->
            handlebars.registerHelper("operationFilter", newOpFilter(select.toString())));
  }

  private Helper<CodegenOperation> newOpFilter(String select) {
    return (context, options) -> {
      final String[] ops = select != null && select.trim().length() > 0
          ? select.split(",")
          : new String[0];

      if (ops.length == 0 || Arrays.stream(ops)
          .noneMatch(op -> op.trim().equals(context.operationId))) {
        return "";
      }
      return options.fn(context);
    };
  }

  private Helper<CodegenOperation> normalizePath() {
    return (ctx, options) -> {
      if (ctx.path.startsWith("/")) {
        ctx.path = ctx.path.substring(1);
      }
      return ctx;
    };
  }

  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + apiFilePath();
  }

  protected String apiFilePath() {
    String pk = invokerPackage != null
        ? invokerPackage
        : apiPackage;
    return sourceFolder + "/" + pk.replace('.', '/');
  }
}
