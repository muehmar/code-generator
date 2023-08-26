package io.github.muehmar.codegenerator.writer;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

public class JavaRefWriter implements RefWriter {
  @Override
  public Comparator<String> sortComparator() {
    return Comparator.comparing(Function.identity());
  }

  @Override
  public Predicate<String> filter() {
    return ref -> !ref.startsWith("java.lang");
  }

  @Override
  public String format(String ref) {
    return String.format("import %s;", ref);
  }
}
