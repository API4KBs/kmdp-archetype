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
package edu.mayo.kmdp.util.ws;

import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthorizationHeaderForwardConfiguration implements WebMvcConfigurer {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Set<String> headers = new HashSet<>();
        headers.add(AUTHORIZATION_HEADER);
        registry.addInterceptor(new HeaderForwardServerInterceptor(headers));
    }

}
