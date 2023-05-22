package edu.mayo.kmdp;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class that handles the parameter ordering in the Swagger/OAS/Java/Spring mappings
 * <p>
 * Addresses the breaking described <a
 * href="https://github.com/swagger-api/swagger-codegen/issues/9103">here</a> and the possible
 * discrepancy between the OAS3 strategy used in Java Clients ("mode C"), Spring delegates ("mode
 * D") and the normalized, more backward compatible KMP strategy ("mode N").
 */
public class ParamsAdapterHelper implements Helper<CodegenOperation> {

  enum MODE {
    C, D, N
  }

  public static final String NAME = "adaptedParams";
  private final Map<String, String> typeMap;

  public ParamsAdapterHelper(Map<String, String> typeMap) {
    this.typeMap = typeMap;
  }

  @Override
  public CharSequence apply(CodegenOperation context, Options options) {
    boolean formal = getFlag(options, "withType", true);
    boolean requiredOnly = getFlag(options, "required", false);
    boolean delegate = getFlag(options, "delegate", false);
    boolean client = getFlag(options, "client", false);

    return sortedParams(context, decode(delegate, client)).stream()
        .map(p -> write(p, formal, requiredOnly))
        .filter(s -> s.length() > 0)
        .collect(Collectors.joining(", "));
  }

  private boolean getFlag(Options options, String flagName, boolean defaultFlag) {
    return Optional.ofNullable(options.hash.getOrDefault(flagName, defaultFlag))
        .map(Objects::toString)
        .map(Boolean::valueOf)
        .orElse(defaultFlag);
  }

  private String write(CodegenParameter p, boolean formal, boolean requiredOnly) {
    return formal
        ? writeFormal(p, requiredOnly)
        : writeActual(p, requiredOnly);
  }

  private String writeFormal(CodegenParameter p, boolean requiredOnly) {
    return !requiredOnly || p.required
        ? getDatatype(p) + " " + p.getParamName()
        : "";
  }

  private String getDatatype(CodegenParameter p) {
    var type = p.dataType;
    return typeMap.getOrDefault(type, type);
  }

  private String writeActual(CodegenParameter p, boolean requiredOnly) {
    return !requiredOnly || p.required
        ? p.getParamName()
        : formatDefault(p);
  }

  private String formatDefault(CodegenParameter p) {
    if (p.getDefaultValue() == null) {
      return "null";
    } else {
      return "String".equals(p.getDataType())
          ? "\"" + p.getDefaultValue() + "\""
          : p.getDefaultValue();
    }
  }

  private List<CodegenParameter> sortedParams(CodegenOperation context, MODE mode) {
    var path = context.getPathParams().stream();
    var query = context.getQueryParams().stream();
    var body = Stream.ofNullable(context.getBodyParam());
    var form = context.getFormParams().stream();
    var header = context.getHeaderParams().stream();
    var cook = context.getCookieParams().stream();

    if (mode == MODE.C) {
      if (context.getHasBodyParam()) {
        return Stream.of(form, body, header, query, path, cook)
            .flatMap(s -> s)
            .sorted((one, another) -> {
              if (one.required == another.required) {
                return 0;
              } else if (one.required) {
                return -1;
              } else {
                return 1;
              }
            }).collect(Collectors.toList());
      } else {
        return new ArrayList<>(context.getAllParams());
      }
    } else if (mode == MODE.D) {
      return Stream.of(body, path, query, header, cook, form)
          .flatMap(s -> s)
          .filter(Objects::nonNull)
          .sorted(Comparator.comparing(k -> indexOf(k, context.contents.get(0).getParameters())))
          .collect(Collectors.toList());
    } else {
      // should be path, query, body?
      return Stream.of(path, body, query, header, form, cook)
          .flatMap(s -> s)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
  }


  private int indexOf(CodegenParameter param, List<CodegenParameter> allParams) {
    return allParams.stream()
        .filter(ap -> Objects.equals(ap.getParamName(), param.getParamName()))
        .map(allParams::indexOf)
        .findFirst()
        .orElseThrow();
  }

  private MODE decode(boolean delegate, boolean client) {
    if (client && delegate) {
      throw new IllegalStateException();
    }
    if (delegate) {
      return MODE.D;
    }
    if (client) {
      return MODE.C;
    }
    return MODE.N;
  }
}