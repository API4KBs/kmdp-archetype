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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelperTest {

  @Test
  public void testSucceedWithBody() {
    ResponseEntity<String> r = ResponseHelper.succeed("ok");
    assertEquals("ok", r.getBody());
  }

  @Test
  public void testSucceed() {
    ResponseEntity<Void> r = ResponseHelper.succeed();
    assertNull(r.getBody());
  }

  @Test
  public void testSucceedWithHeader() {
    ResponseEntity<Void> r = ResponseHelper.succeed(
        URI.create("http://foo.bar"),
        HttpStatus.CREATED,
        HttpHeaders::setLocation);
    assertNull(r.getBody());
    assertEquals(URI.create("http://foo.bar"),
        r.getHeaders().getLocation());
  }

  @Test
  public void testIllegalState() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ResponseHelper.succeed(HttpStatus.NOT_FOUND);
    });
  }

}
