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

import com.samskivert.mustache.Mustache.Lambda;
import com.samskivert.mustache.Template.Fragment;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.AbstractJavaCodegen;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;

public class FnappHttpServerGenerator extends AbstractJavaCodegen implements CodegenConfig {

  public static final String FNAPP_J = "JavaFnApp";
  private static final String SELECTED_OPERATIONS = "selectedOperations";

  public FnappHttpServerGenerator() {
    super();

    cliOptions.add(
        CliOption.newString(SELECTED_OPERATIONS,
            "Comma-separated list of operation IDs"));

    embeddedTemplateDir = templateDir = FNAPP_J;
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

    supportsInheritance = true;

    modelTemplateFiles.clear();
    apiTestTemplateFiles.clear();
    modelDocTemplateFiles.clear();
    apiDocTemplateFiles.clear();
    supportingFiles.clear();
    apiTemplateFiles.clear();

    supportingFiles.add(new SupportingFile("java-fnapp-factory.mustache", apiFilePath(), "ProviderFactory.java"));

    apiTemplateFiles.put("java-fnapp-http.mustache", "Function.java");
    apiTemplateFiles.put("java-fnapp-service-provider.mustache", "Provider.java");

    supportedLibraries.put(FNAPP_J, "Java Azure Function App Shell");
    setLibrary(FNAPP_J);

    Optional.ofNullable(additionalProperties.get(SELECTED_OPERATIONS))
        .ifPresent(select ->
            additionalProperties.put("operationFilter", newOpFilter(select.toString())));
  }

  private Lambda newOpFilter(String select) {
    return new Lambda() {
      final String[] ops = select != null && select.trim().length() > 0
          ? select.split(",")
          : new String[0];

      @Override
      public void execute(Fragment fragment, Writer writer) throws IOException {
        if (ops.length > 0 && fragment.context() instanceof CodegenOperation) {
          CodegenOperation ctx = (CodegenOperation) fragment.context();
          if (Arrays.stream(ops).noneMatch(op -> op.trim().equals(ctx.operationId))) {
            return;
          }
        }
        fragment.execute(writer);
      }
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
