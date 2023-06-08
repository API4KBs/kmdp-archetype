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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderForwardClientInterceptorTest {

    @Test
    public void testSetHeader() throws Exception {
        Map<String, String> testHeaders = Maps.newHashMap();
        testHeaders.put("Test", "1");

        WebSessionContext.setHeaders(testHeaders);

        HeaderForwardClientInterceptor interceptor = new HeaderForwardClientInterceptor(Sets.newHashSet("Test"));

        interceptor.intercept(new MockClientHttpRequest(), null, (httpRequest, bytes) -> {
            assertEquals("1", httpRequest.getHeaders().get("Test").get(0));
            return null;
        });
    }

    @Test
    public void testSetHeaderNonForwarded() throws Exception {
        Map<String, String> testHeaders = Maps.newHashMap();
        testHeaders.put("Test", "1");

        WebSessionContext.setHeaders(testHeaders);

        HeaderForwardClientInterceptor interceptor = new HeaderForwardClientInterceptor(Sets.newHashSet("NotTest"));

        interceptor.intercept(new MockClientHttpRequest(), null, (httpRequest, bytes) -> {
            assertNull(httpRequest.getHeaders().get("Test"));
            return null;
        });
    }

    @Test
    public void testSetHeaderOnlyRequested() throws Exception {
        Map<String, String> testHeaders = Maps.newHashMap();
        testHeaders.put("TestA", "A");
        testHeaders.put("TestB", "B");

        WebSessionContext.setHeaders(testHeaders);

        HeaderForwardClientInterceptor interceptor = new HeaderForwardClientInterceptor(Sets.newHashSet("TestB"));

        interceptor.intercept(new MockClientHttpRequest(), null, (httpRequest, bytes) -> {
            assertEquals("B", httpRequest.getHeaders().get("TestB").get(0));
            assertNull(httpRequest.getHeaders().get("TestA"));
            return null;
        });
    }

}