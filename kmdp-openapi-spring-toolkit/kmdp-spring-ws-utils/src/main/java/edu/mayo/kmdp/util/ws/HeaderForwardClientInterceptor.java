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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HeaderForwardClientInterceptor implements ClientHttpRequestInterceptor {

    private Set<String> headersToForward;

    public HeaderForwardClientInterceptor(Set<String> headersToForward) {
        this.headersToForward = new HashSet<>(headersToForward);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Map<String, String> headers = WebSessionContext.getHeaders();

        if (headers != null) {
            headers.entrySet().stream()
                    .filter(entry -> this.headersToForward.contains(entry.getKey()))
                    .forEach(entry -> request.getHeaders().add(entry.getKey(), entry.getValue()));
        }

        return execution.execute(request, body);
    }

}
