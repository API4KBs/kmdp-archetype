/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.util.ws;

import static org.omg.spec.api4kp._20200801.Explainer.packExplanationIntoHeaders;

import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.Answer;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ResponseHelper {

  private static Logger logger = LoggerFactory.getLogger(ResponseHelper.class);

  private ResponseHelper() {
  }

  public static <T> List<T> getAll(ResponseEntity<List<T>> resp) {
    return isSuccess(resp) ? resp.getBody() : Collections.emptyList();
  }

  public static <T> List<T> aggregate(ResponseEntity<List<T>> resp) {
    return isSuccess(resp) ? resp.getBody() : Collections.emptyList();
  }


  public static <T> Optional<T> get(ResponseEntity<T> resp) {
    return isSuccess(resp) ? Optional.ofNullable(resp.getBody()) : Optional.empty();
  }


  public static boolean isSuccess(ResponseEntity<?> resp) {
    return resp.getStatusCode().is2xxSuccessful();
  }


  public static ResponseEntity<Void> succeed() {
    return ResponseEntity.ok().build();
  }

  public static ResponseEntity<Void> succeed(HttpStatus status) {
    checkStatus(status, HttpStatus::is2xxSuccessful);
    return new ResponseEntity<>(status);
  }

  public static <T> ResponseEntity<T> succeed(T result) {
    return result == null
        ? ResponseEntity.ok().build()
        : ResponseEntity.ok(result);
  }

  public static <T> ResponseEntity<T> succeed(T result, HttpStatus status) {
    checkStatus(status, HttpStatus::is2xxSuccessful);
    return new ResponseEntity<>(result, status);
  }

  public static <T> ResponseEntity<Void> succeed(T result, HttpStatus status,
      BiConsumer<HttpHeaders, T> headerSetter) {
    checkStatus(status, HttpStatus::is2xxSuccessful);
    HttpHeaders httpHeaders = new HttpHeaders();
    headerSetter.accept(httpHeaders, result);
    return new ResponseEntity<>(httpHeaders, status);
  }

  public static <T> ResponseEntity<T> notSupported() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public static <T> ResponseEntity<T> fail() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }


  public static <T> ResponseEntity<T> attempt(T result) {
    return attempt(Optional.ofNullable(result));
  }

  public static <T> ResponseEntity<T> attempt(Optional<T> result) {
    return result
        .map(ResponseHelper::succeed)
        .orElse(ResponseEntity.notFound().build());
  }

  public static <X, T> ResponseEntity<T> delegate(Optional<X> delegate,
      Function<X, ResponseEntity<T>> fun) {
    return delegate
        .map(fun)
        .orElse(notSupported());
  }

  public static <X, T> List<T> aggregate(Collection<X> delegates,
      Function<X, ResponseEntity<List<T>>> mapper) {
    return delegates.stream()
        .map(mapper)
        .map(ResponseHelper::getAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public static <X, T> Optional<T> anyDo(Collection<X> delegates,
      Function<X, ResponseEntity<T>> mapper) {
    return delegates.stream()
        .map(mapper)
        .map(ResponseHelper::get)
        .flatMap(StreamUtil::trimStream)
        .findAny();
  }

  public static <T> boolean matches(ResponseEntity<T> resp, Predicate<T> condition) {
    return get(resp).filter(condition).isPresent();
  }

  public static <X, T> ResponseEntity<T> map(ResponseEntity<X> resp, Function<X, T> mapper) {
    return attempt(get(resp)
        .map(mapper));
  }

  public static <X, T> ResponseEntity<T> flatMap(ResponseEntity<X> resp,
      Function<X, ResponseEntity<T>> mapper) {
    return get(resp)
        .map(mapper)
        .orElse(fail());
  }

  public static <X> Optional<X> anyAble(Collection<X> delegates, Predicate<X> filter) {
    return delegates.stream()
        .filter(filter)
        .findAny();
  }

  public static <X> ResponseEntity<List<X>> collect(
      Stream<ResponseEntity<X>> responses) {
    return succeed(
        responses
            .map(ResponseHelper::get)
            .flatMap(StreamUtil::trimStream)
            .collect(Collectors.toList()));
  }


  private static void checkStatus(HttpStatus status, Predicate<HttpStatus> test) {
    if (test != null && !test.test(status)) {
      throw new IllegalArgumentException(
          "HTTP Status " + status + "is not adequate in this context");
    }
  }

  public static <T> ResponseEntity<T> asResponse(Answer<T> ans) {
    return new ResponseEntity<>(
        ans.getOptionalValue().orElse(null),
        mapHeaders(ans),
        mapCode(ans.getOutcomeType())
    );
  }

  public static <T> Answer<T> asAnswer(ResponseEntity<T> response) {
    if (response == null) {
      return Answer.<T>failed().withAddedExplanationMessage("Null response");
    }
    return Answer.of(
        mapStatus(response.getStatusCode()),
        response.getBody(),
        response.getHeaders());
  }

  public static ResponseCode mapStatus(HttpStatus statusCode) {
    return ResponseCodeSeries.resolveTag(Integer.toString(statusCode.value()))
        .orElseThrow(() ->
            new IllegalArgumentException("Unexpected status code " + statusCode.value()));
  }


  private static MultiValueMap<String, String> mapHeaders(Answer<?> ans) {
    HttpHeaders headers = new HttpHeaders();
    ans.listMeta().forEach(h -> headers.addAll(
        Encode.forHtml(h),
        ans.getMetas(h).stream().map(Encode::forHtml).collect(Collectors.toList())));
    packExplanationIntoHeaders(ans, headers);
    return headers;
  }

  private static HttpStatus mapCode(ResponseCode outcomeType) {
    if (outcomeType == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      int statusCode = Integer.parseInt(outcomeType.getTag());
      return HttpStatus.resolve(statusCode);
    } catch (NumberFormatException nfe) {
      logger.error(nfe.getMessage(), nfe);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public static <T> ResponseEntity<T> redirectTo(URI locator) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setLocation(locator);
    return new ResponseEntity<>(null, httpHeaders, HttpStatus.SEE_OTHER);
  }
}
