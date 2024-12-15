package io.github.muehmar.codegenerator.writer;

import static org.assertj.core.api.Assertions.assertThat;

import ch.bluecare.commons.data.PList;
import org.junit.jupiter.api.Test;

class LineTest {
  @Test
  void append_when_calledWithAdditionalFragment_then_fragmentAppended() {
    final Line helloWorld = Line.ofString("Hello").append(" World!");
    assertThat(helloWorld.asString()).isEqualTo("Hello World!");
  }

  @Test
  void prepend_when_calledWithAdditionalFragment_then_fragmentPrepended() {
    final Line helloWorld = Line.ofString("World!").prepend("Hello ");
    assertThat(helloWorld.asString()).isEqualTo("Hello World!");
  }

  @Test
  void prependList_when_calledWithAdditionalFragments_then_fragmentsPrepended() {
    final Line helloWorld = Line.ofString("World!").prepend(PList.of("Hello", " "));
    assertThat(helloWorld.asString()).isEqualTo("Hello World!");
  }

  @Test
  void
      removeTrailingBlankFragments_when_lineWithDifferentBlankFragments_then_trailingBlankFragmentsRemoved() {
    final Line line =
        Line.ofString(" ").append("Hello").append(" ").append("World!").append("  ").append(" ");

    assertThat(line.removeTrailingBlankFragments().asString()).isEqualTo(" Hello World!");
  }
}
