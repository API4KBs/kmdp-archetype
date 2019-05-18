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

import edu.mayo.kmdp.util.FileUtil;
import java.io.File;

public class ClientTemplateGenerator {

  public static void main(String... args) {
    File src = new File(args[0]);
    File tgt = new File(args[1]);

    FileUtil.read(src)
        .map(ClientTemplateGenerator::rewrite)
        .ifPresent((f) -> FileUtil.write(f, tgt));
  }

  private static String rewrite(String s) {
    return s.replaceAll("\\{\\{>licenseInfo}}", "")
        .replaceAll("\\{\\{>generatedAnnotation}}", "")
        .replaceAll("\\{\\{classname}}",
            "\\{\\{classname\\}\\}Responsive")
        .replaceAll("\\{\\{invokerPackage}}.ApiClient;",
            "\\{\\{package\\}\\}.ResponsiveApiClient; \n"
                + "import org.springframework.http.ResponseEntity;\n")
        .replaceAll("ApiClient ",
            "ResponsiveApiClient ")
        .replaceAll("new ApiClient",
            "ResponsiveApiClient.newInstance")
        .replaceAll("public \\{\\{#returnType}}",
            "public ResponseEntity<\\{\\{#returnType\\}\\}")
        .replaceAll("void \\{\\{/returnType}}",
            "Void \\{\\{/returnType}}> ")
        .replaceAll("\\{\\{#returnType}}return \\{\\{/returnType}}",
            "return ")
        .replaceAll("invokeAPI",
            "callAPI");
  }

}
