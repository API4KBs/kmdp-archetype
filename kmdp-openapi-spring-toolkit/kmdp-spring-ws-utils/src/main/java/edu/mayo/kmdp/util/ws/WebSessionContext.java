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


import java.util.HashMap;
import java.util.Map;

public class WebSessionContext {

    protected WebSessionContext() {}

    private static final ThreadLocal<Map<String,String>> CONTEXT = new ThreadLocal<>();

    public static void setHeaders(Map<String,String> headers) {
        CONTEXT.set(new HashMap<>(headers));
    }

    public static Map<String,String> getHeaders() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}