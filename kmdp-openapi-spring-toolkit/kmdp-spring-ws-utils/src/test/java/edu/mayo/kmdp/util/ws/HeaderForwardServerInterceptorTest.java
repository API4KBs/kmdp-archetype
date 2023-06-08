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

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderForwardServerInterceptorTest {

    @Test
    public void setHeader() {
        HashSet<String> headers = Sets.newHashSet("Test");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Test", "1");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals("1", WebSessionContext.getHeaders().get("Test"));

    }

    @Test
    public void setHeaderNotInList() {
        HashSet<String> headers = Sets.newHashSet("XXXXX");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Test", "1");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertNull(WebSessionContext.getHeaders().get("Test"));

    }

    @Test
    public void setHeaderInListButNull() {
        HashSet<String> headers = Sets.newHashSet("Test");

        HeaderForwardServerInterceptor interceptor = new HeaderForwardServerInterceptor(headers);

        MockHttpServletRequest request = new MockHttpServletRequest();

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertNull(WebSessionContext.getHeaders().get("Test"));

    }

}