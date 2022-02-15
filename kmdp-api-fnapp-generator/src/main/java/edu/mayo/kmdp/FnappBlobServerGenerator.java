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

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.AbstractJavaCodegen;

public class FnappBlobServerGenerator extends AbstractJavaCodegen implements CodegenConfig {

  public static final String FNAPP_J = "JavaFnApp";
  private static final String CONNECTION_STRING = "connectionString";
  private static final String STORAGE_PATH = "storagePath";
  private static final String MIME_TYPE = "mimeType";

  public FnappBlobServerGenerator() {
    super();

    cliOptions.add(
        CliOption.newString(CONNECTION_STRING,
            ""));
    cliOptions.add(
        CliOption.newString(STORAGE_PATH,
            ""));
    cliOptions.add(
        CliOption.newString(MIME_TYPE,
            ""));

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

    supportingFiles.add(
        new SupportingFile("java-fnapp-blob.mustache", apiFilePath(), "BlobFunction.java"));

    supportedLibraries.put(FNAPP_J, "Java Azure Function App Shell");
    setLibrary(FNAPP_J);

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
