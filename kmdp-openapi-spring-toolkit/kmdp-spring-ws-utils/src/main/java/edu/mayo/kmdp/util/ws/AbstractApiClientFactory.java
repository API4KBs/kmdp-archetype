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

import static edu.mayo.ontology.taxonomies.ws.responsecodes._2011.ResponseCode.InternalServerError;

import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractApiClientFactory {

  protected RestTemplate restTemplate;

  protected String basePath;

  protected AbstractApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir) {
    this(basePath, withFhir, new HashSet<>(
        Collections.singletonList(AuthorizationHeaderForwardConfiguration.AUTHORIZATION_HEADER)));
  }

  protected AbstractApiClientFactory(String basePath, JsonRestWSUtils.WithFHIR withFhir, Set<String> forwardHeaders) {
    this.basePath = basePath;
    RestTemplate template = this.buildDefaultRestTemplate();

    HeaderForwardClientInterceptor interceptor =
        new HeaderForwardClientInterceptor(forwardHeaders);

    template.getInterceptors().add(interceptor);

    this.restTemplate = JsonRestWSUtils.enableFHIR(template, withFhir);
  }

  protected AbstractApiClientFactory(String basePath, RestTemplate restTemplate) {
    this.basePath = basePath;
    this.restTemplate = restTemplate;
  }


  protected RestTemplate buildDefaultRestTemplate() {
    RestTemplate template = new RestTemplate();
    // This allows us to read the response more than once - Necessary for debugging.
    template.setRequestFactory(new BufferingClientHttpRequestFactory(template.getRequestFactory()));
    template.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      protected void handleError(ClientHttpResponse response, HttpStatus statusCode)
          throws IOException {
        throw new ServerSideException(
            ResponseCodeSeries.resolve(Integer.toString(response.getRawStatusCode()))
                .orElse(InternalServerError),
            response.getHeaders(),
            getResponseBody(response));
      }
    });
    List<HttpMessageConverter<?>> converters = template.getMessageConverters();
    converters.add(new HTMLKnowledgeCarrierWrapper());
    template.setMessageConverters(converters);
    return template;
  }
}
