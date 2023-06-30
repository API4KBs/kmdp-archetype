package edu.mayo.kmdp.util.ws;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathParamList<T> extends java.util.ArrayList<T> {

  public static <T> List<T> wrap(List<T> arg) {
    return new PathParamList<>(arg);
  }

  public PathParamList(Collection<? extends T> coll) {
    super(coll);
  }

  @Override
  public String toString() {
    return this.stream()
        .filter(Objects::nonNull)
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }
}