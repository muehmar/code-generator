package io.github.muehmar.codegenerator.java;

import static org.assertj.core.api.Assertions.assertThat;

import ch.bluecare.commons.data.PList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaModifiersTest {

  @ParameterizedTest
  @MethodSource("privateStaticFinalModifiersMultipleTimesAndDifferentOrder")
  void
      asString_when_privateStaticFinalModifiersMultipleTimesAndDifferentOrder_then_singleOccurenceAndCorrectOrder(
          PList<JavaModifier> modifiers) {
    assertThat(JavaModifiers.of(modifiers).asString()).isEqualTo("private static final");
  }

  private static Stream<Arguments> privateStaticFinalModifiersMultipleTimesAndDifferentOrder() {
    return Stream.of(
        Arguments.of(PList.of(JavaModifier.FINAL, JavaModifier.PRIVATE, JavaModifier.STATIC)),
        Arguments.of(PList.of(JavaModifier.FINAL, JavaModifier.STATIC, JavaModifier.PRIVATE)),
        Arguments.of(PList.of(JavaModifier.STATIC, JavaModifier.FINAL, JavaModifier.PRIVATE)),
        Arguments.of(PList.of(JavaModifier.PRIVATE, JavaModifier.FINAL, JavaModifier.STATIC)),
        Arguments.of(PList.of(JavaModifier.PRIVATE, JavaModifier.STATIC, JavaModifier.FINAL)),
        Arguments.of(PList.of(JavaModifier.STATIC, JavaModifier.PRIVATE, JavaModifier.FINAL)),
        Arguments.of(
            PList.of(
                JavaModifier.STATIC,
                JavaModifier.FINAL,
                JavaModifier.PRIVATE,
                JavaModifier.FINAL,
                JavaModifier.STATIC)));
  }

  @Test
  void asStringTrailingWhiteSpace_when_noModifiers_then_outputEmpty() {
    final JavaModifiers modifiers = JavaModifiers.of();
    assertThat(modifiers.asStringTrailingWhitespace()).isEqualTo("");
  }

  @Test
  void asStringTrailingWhiteSpace_when_atLeastOneModifier_then_outputWithTrailingWhitespace() {
    final JavaModifiers modifiers = JavaModifiers.of(JavaModifier.PRIVATE);
    assertThat(modifiers.asStringTrailingWhitespace()).isEqualTo("private ");
  }
}
