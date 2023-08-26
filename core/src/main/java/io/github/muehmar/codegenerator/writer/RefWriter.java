package io.github.muehmar.codegenerator.writer;

import java.util.Comparator;
import java.util.function.Predicate;

public interface RefWriter {
  Comparator<String> sortComparator();

  Predicate<String> filter();

  String format(String ref);
}
