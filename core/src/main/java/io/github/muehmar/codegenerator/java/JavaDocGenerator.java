package io.github.muehmar.codegenerator.java;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.util.Strings;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.function.BiFunction;

public class JavaDocGenerator {
  private static final int MAX_LENGTH = 80;

  private JavaDocGenerator() {}

  public static <A, B> Generator<A, B> javaDoc(BiFunction<A, B, String> javaDoc) {
    return (a, b, writer) -> javaDoc().generate(javaDoc.apply(a, b), b, writer);
  }

  public static <A, B> Generator<A, B> ofJavaDocString(String javaDoc) {
    return (a, b, writer) -> javaDoc().generate(javaDoc, b, writer);
  }

  public static <B> Generator<String, B> javaDoc() {
    return Generator.<String, B>ofWriterFunction(w -> w.println("/**"))
        .append(content())
        .append(w -> w.println(" */"))
        .filter(Strings::nonEmptyOrBlank);
  }

  private static <B> Generator<String, B> content() {
    return (input, ign, writer) -> {
      final String[] lines = input.split("\n");
      return PList.fromArray(lines)
          .zipWithIndex()
          .map(p -> lines.length == p.second() + 1 ? p.first() : p.first().concat("<br>"))
          .flatMap(JavaDocGenerator::autoNewline)
          .map(line -> " * " + line)
          .foldLeft(writer, Writer::println);
    };
  }

  private static PList<String> autoNewline(String line) {
    if (line.trim().isEmpty()) {
      return PList.empty();
    }

    if (line.length() <= MAX_LENGTH) {
      return PList.single(line);
    }

    final int newLineIndex = determineNewLineIndex(line);

    final String firstLine = line.substring(0, newLineIndex);
    if (firstLine.equals(line)) {
      return PList.single(firstLine);
    }
    final String remaining = line.substring(newLineIndex + 1);
    return autoNewline(remaining).cons(firstLine);
  }

  private static int determineNewLineIndex(String line) {
    final int lastWhiteSpaceWithinMaxLength = line.substring(0, MAX_LENGTH + 1).lastIndexOf(" ");
    if (lastWhiteSpaceWithinMaxLength > 0) {
      return lastWhiteSpaceWithinMaxLength;
    }

    final int firstWhitespace = line.indexOf(" ");
    if (firstWhitespace > 0) {
      return firstWhitespace;
    }

    return line.length();
  }
}
