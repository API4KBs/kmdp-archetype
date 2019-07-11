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
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.AbstractJavaCodegen;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class FacadeClientGenerator extends AbstractJavaCodegen implements CodegenConfig {

  public FacadeClientGenerator() {
    super();


    cliOptions
        .add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
    cliOptions.add(new CliOption(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));
    cliOptions.add(
        new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
    cliOptions.add(new CliOption(CodegenConstants.GROUP_ID, CodegenConstants.GROUP_ID_DESC));
    cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_ID, CodegenConstants.ARTIFACT_ID_DESC));
    cliOptions.add(
        new CliOption(CodegenConstants.ARTIFACT_VERSION, CodegenConstants.ARTIFACT_VERSION_DESC));
    cliOptions
        .add(new CliOption(CodegenConstants.ARTIFACT_URL, CodegenConstants.ARTIFACT_URL_DESC));
    cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_DESCRIPTION,
        CodegenConstants.ARTIFACT_DESCRIPTION_DESC));
    cliOptions
        .add(new CliOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC));
    cliOptions.add(new CliOption(CodegenConstants.LOCAL_VARIABLE_PREFIX,
        CodegenConstants.LOCAL_VARIABLE_PREFIX_DESC));
    cliOptions.add(CliOption
        .newBoolean(CodegenConstants.SERIALIZABLE_MODEL, CodegenConstants.SERIALIZABLE_MODEL_DESC));
    cliOptions
        .add(CliOption.newBoolean(CodegenConstants.SERIALIZE_BIG_DECIMAL_AS_STRING, CodegenConstants
            .SERIALIZE_BIG_DECIMAL_AS_STRING_DESC));
    cliOptions.add(CliOption.newBoolean(FULL_JAVA_UTIL,
        "whether to use fully qualified name for classes under java.util. This option only works for Java API client"));

    //templateDir = initTemplateDir();
  }


  @Override
  public void processOpts() {
    super.processOpts();

    supportsInheritance = true;
    modelTemplateFiles.clear();
    apiTemplateFiles.clear();
    apiTestTemplateFiles.clear();
    modelDocTemplateFiles.clear();
    apiDocTemplateFiles.clear();
    supportingFiles.clear();

    apiTemplateFiles.put("java-client-facade.mustache", ".java");
    apiTemplateFiles.put("java-client-facade-impl.mustache", "Facade.java");

  }

  @Override
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  @Override
  public String getName() {
    return "java-vm";
  }

  @Override
  public String getHelp() {
    return "TODO";
  }

  @Override
  public Map<String, String> apiTemplateFiles() {
    return super.apiTemplateFiles();
  }

  @Override
  public List<SupportingFile> supportingFiles() {
    return super.supportingFiles();
  }
}
