package edu.mayo.kmdp.function.util;

import static edu.mayo.kmdp.util.CharsetEncodingUtil.recodeToBase64;
import static edu.mayo.kmdp.util.Util.isEmpty;
import static edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries.BadRequest;
import static org.omg.spec.api4kp._20200801.Explainer.EXPL_HEADER;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_ERROR_TYPE;

import com.google.cloud.functions.HttpResponse;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.owasp.encoder.Encode;

/**
 * Helper methods to bridge the API4KP / Azure Fn'App layers
 */
public class CloudFunctionHelper {

  private CloudFunctionHelper() {
    // functions only
  }

  /**
   * Extracts a Path parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param path         the actual request path
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> T fromPath(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      String path,
      String requestId) {

    String value = null;
    String key = "{" + argName + "}";

    String[] routeBlocks = route.split("/");
    String[] pathBlocks = path.split("/");

    for (int j = 0; j < routeBlocks.length; j++) {
      if (key.equals(routeBlocks[j])) {
        value = pathBlocks[j];
        break;
      }
    }

    // path args are mandatory
    return tryParse(
        argName,
        value != null ? List.of(value) : List.of(),
        argType, defaultValue, required, requestId,
        CloudFunctionHelper::parseSimple).get(0);
  }

  /**
   * Extracts a Body parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param body         the body of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> List<T> fromBody(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      String body,
      String requestId,
      BiFunction<String, Class<T>, T> parser) {

    return tryParse(argName, List.of(body), argType, defaultValue, required, requestId, parser);
  }

  /**
   * Extracts a Header parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param headers      the headers of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> List<T> fromHeader(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      Map<String, List<String>> headers,
      String requestId) {

    return tryParse(argName, headers.get(argName), argType, defaultValue, required, requestId,
        CloudFunctionHelper::parseSimple);
  }

  /**
   * Extracts a Query parameter from a request
   *
   * @param argName      the name of the parameter
   * @param route        the path pattern
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param query        the query parameters of the request
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  public static <T> List<T> fromQuery(
      String argName,
      String route,
      Class<T> argType,
      boolean required,
      String defaultValue,
      Map<String, List<String>> query,
      String requestId) {

    return tryParse(argName, query.get(argName), argType, defaultValue, required, requestId,
        CloudFunctionHelper::parseSimple);
  }

  /**
   * Converts an {@link Answer} to a {@link HttpResponse}
   * <p>
   * Maps the HTTP status code and return headers (if any).
   * <p>
   * If {@link Answer#isSuccess()}, serializes the body in JSON and sets the Content-Type header.
   * <p>
   * //TODO this may break the type-safe contract.. problem may be better as a header.. Otherwise,
   * uses the Answer's explanation {@link org.zalando.problem.Problem} as a return type
   */
  public static HttpResponse toResponse(
      FunctionContext context,
      HttpResponse response,
      Answer<?> answer) {
    response.setStatusCode(Integer.parseInt(answer.getOutcomeType().getTag()));

    try {
      response.appendHeader("Content-Type", "application/json");
      var out = response.getWriter();
      if (answer.isSuccess()) {
        if (answer.has(String.class)) {
          out.write((String) answer.get());
        } else {
          out.write(answer.flatOpt(JSonUtil::writeJsonAsString).get());
        }
      } else {
        out.write(answer. printExplanationAsJson());
      }
      out.flush();
    } catch (Exception e) {
      logException(context, e);
    }

    var expl = recodeToBase64(answer.printExplanationAsJson());
    response.appendHeader(EXPL_HEADER, expl);

    answer.listMeta().forEach(h ->
        answer.getMetas(h)
            .forEach(v ->
                response.appendHeader(Encode.forHtml(h), Encode.forHtml(v))));

    return response;
  }


  /**
   * Validates and parses an input parameter, assuming that value is a String serialization of an
   * object of type T
   *
   * @param argName      the name of the parameter
   * @param value        the value of the parameter
   * @param argType      the parameter datatype
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @param requestId    unique identifier
   * @param <T>          the parameter datatype
   * @return T, or null
   * @throws ServerSideException (BadRequest) if unable to parse the parameter value as a T
   */
  private static <T> List<T> tryParse(
      String argName,
      List<String> value,
      Class<T> argType,
      String defaultValue,
      boolean required,
      String requestId,
      BiFunction<String, Class<T>, T> parser) {

    List<String> validatedValue;
    try {
      validatedValue = validateRaw(argName, value, defaultValue, required);
    } catch (Exception e) {
      throw wrapException(BadRequest, e, requestId);
    }
    if (validatedValue == null) {
      return Collections.emptyList();
    }

    try {
      return validatedValue.stream()
          .map(v -> parser.apply(v, argType)).collect(Collectors.toList());
    } catch (Exception e) {
      throw wrapException(BadRequest, e, requestId);
    }

  }


  public static <T> T parseSimple(String value, Class<T> argType) {

    // TODO add more specialized constructors here if needed...
    if (UUID.class.equals(argType)) {
      try {
        return argType.cast(UUID.fromString(value));
      } catch (Exception e) {
        throw wrapException(
            BadRequest,
            e,
            UUID.randomUUID().toString());
      }
    }

    try {
      return argType.getConstructor(String.class).newInstance(value);
    } catch (Exception e) {
      return JSonUtil.parseJson(value, argType)
          .orElseThrow(() -> wrapException(
              BadRequest,
              new IllegalArgumentException("Unable to parse " + value),
              UUID.randomUUID().toString()));
    }
  }

  /**
   * Checks whether an argument is present (not null, blanks are allowed though), returning the
   * defaultValue otherwise. If the argument is required but no defaultValue is provided, throws an
   * Exception (expected to be caught and recast as a BadRequest error)
   *
   * @param argName      the name of the parameter
   * @param value        the value of the parameter
   * @param required     true if mandatory
   * @param defaultValue used if absent
   * @return the value if present, defaultValue otherwise
   */
  private static List<String> validateRaw(
      String argName,
      List<String> value,
      String defaultValue,
      boolean required) {
    var hasClientValue = value != null && ! value.isEmpty();
    if (required && !hasClientValue && isEmpty(defaultValue)) {
      var message = String.format("Required parameter '%s' not present", argName);
      throw new IllegalArgumentException(message);
    }
    return hasClientValue ? value : List.of(defaultValue);
  }

  /**
   * Logs an Exception using the context Logger
   *
   * @param context the context providing the Logger
   * @param e       the {@link Exception} to be logged
   */
  public static void logException(FunctionContext context, Throwable e) {
    context.getLogger().error(
        "Error in {} - request {} with details : {}",
        context.getFunctionName(),
        context.getInvocationId(),
        e.getMessage());
  }


  /**
   * Wraps a generic Exception into a {@link org.zalando.problem.Problem} enabled Exception
   *
   * @param e         the {@link Exception} to be wrapped
   * @param requestId the Id of the request contextual to the exception being thrown
   * @return a {@link ServerSideException}
   */
  public static RuntimeException wrapException(ResponseCode code, Exception e, String requestId) {
    return new ServerSideException(
        GENERIC_ERROR_TYPE,
        e.getClass().getSimpleName(),
        code,
        e.getMessage(),
        URI.create("urn:" + requestId));
  }

  public static Optional<String> getHeader(HttpResponse httpResponse, String headerContentKey) {
    var l = httpResponse.getHeaders().getOrDefault(headerContentKey, Collections.emptyList());
    return l.isEmpty() ? Optional.empty() : Optional.ofNullable(l.get(0));
  }
}
