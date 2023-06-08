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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WebSessionContextTest {

    @Test
    public void testSetAndGetHeaderSameThread() {
        Map<String, String> testHeaders = Maps.newHashMap();
        testHeaders.put("Test", "1");

        WebSessionContext.setHeaders(testHeaders);

        assertEquals(testHeaders, WebSessionContext.getHeaders());
    }

    @Test
    public void testSetAndGetHeaderDifferentThread() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> t1 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "1");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        Callable<Void> t2 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "2");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        executor.invokeAll(Lists.newArrayList(t1, t2)).forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
    }

    @Test
    public void testSetAndGetHeaderDifferentThreadOverlap() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> t1 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "1");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        Callable<Void> t2 = () -> {
            Map<String, String> testHeaders = Maps.newHashMap();
            testHeaders.put("Test", "2");

            WebSessionContext.setHeaders(testHeaders);

            assertEquals(testHeaders, WebSessionContext.getHeaders());

            return null;
        };

        executor.invokeAll(Lists.newArrayList(t1, t2)).forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
    }

}
