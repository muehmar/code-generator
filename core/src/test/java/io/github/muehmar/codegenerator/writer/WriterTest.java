package io.github.muehmar.codegenerator.writer;

import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WriterTest {
  @Test
  void print_when_formatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = javaWriter().print("Format %s and %s!", "this", "that");
    assertThat(writer.asString()).isEqualTo("Format this and that!");
  }

  @Test
  void tabAndPrint_when_tabAndFormatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = javaWriter().tab(1).print("Format %s and %s!", "this", "that");
    assertThat(writer.asString()).isEqualTo("  Format this and that!");
  }

  @Test
  void print_when_calledMultipleTimes_then_everythingOnOneLine() {
    final Writer writer =
        javaWriter()
            .print("Format %s and %s!", "this", "that")
            .print(" And put")
            .print(" everything on the same")
            .print(" line.");
    assertThat(writer.asString())
        .isEqualTo("Format this and that! And put everything on the same line.");
  }

  @Test
  void println_when_calledTwoTimes_then_twoLinesCreated() {
    final Writer writer = javaWriter().println("Line number %d", 1).println("Line number %d", 2);
    assertThat(writer.asString()).isEqualTo("Line number 1\nLine number 2");
  }

  @Test
  void tabAndPrintln_when_tabCalledBeforePrintLn_then_tabResettedAfterPrintLnCalled() {
    final Writer writer = javaWriter().tab(2).println("First line").println("Second line");
    assertThat(writer.asString()).isEqualTo("    First line\n" + "Second line");
  }

  @Test
  void refAndPrintRefs_when_bothCalled_then_refsPrintedAndOrderedCorrectly() {
    final Writer writer =
        javaWriter()
            .println("First line")
            .printRefs()
            .println("Second line")
            .ref("Ref B")
            .println("Third line")
            .ref("Ref C")
            .ref("Ref A");
    assertThat(writer.asString())
        .isEqualTo(
            "First line\n"
                + "import Ref A;\n"
                + "import Ref B;\n"
                + "import Ref C;\n"
                + "Second line\n"
                + "Third line");
  }

  @Test
  void append_when_calledForDifferentWriters_then_allLinesAppendedOnNewLines() {
    final Writer writerA = javaWriter().println("Something with a newline");
    final Writer writerB = javaWriter().print("Something without a newline");

    final Writer writer =
        javaWriter()
            .println("First line of main writer")
            .append(writerA)
            .print("Line after writer A")
            .append(writerB)
            .println("Line after writer B");
    assertThat(writer.asString())
        .isEqualTo(
            "First line of main writer\n"
                + "Something with a newline\n"
                + "Line after writer A\n"
                + "Something without a newline\n"
                + "Line after writer B");
  }

  @Test
  void append_when_calledWithTabs_then_correctIndentionForAppendedContent() {
    final Writer writerA = javaWriter().println("Content writer A");

    final Writer writer =
        javaWriter()
            .println("First line of main writer")
            .append(2, writerA)
            .println("Some other line");
    assertThat(writer.asString())
        .isEqualTo("First line of main writer\n" + "    Content writer A\nSome other line");
  }

  @Test
  void append_when_writersPrintAndAddRefs_then_allRefsPrinted() {
    final Writer writerA = javaWriter().println("Something of writer A").ref("Writer A ref");

    final Writer writer =
        javaWriter()
            .println("First line of main writer")
            .printRefs()
            .append(writerA)
            .println("Line after writer A")
            .ref("Main writer ref");
    assertThat(writer.asString())
        .isEqualTo(
            "First line of main writer\n"
                + "import Main writer ref;\n"
                + "import Writer A ref;\n"
                + "Something of writer A\n"
                + "Line after writer A");
  }

  @Test
  void asString_when_blankLineAppended_then_tabsRemovedOfBlankLine() {
    final String output =
        javaWriter()
            .tab(1)
            .println("First line")
            .tab(1)
            .println("  ")
            .tab(1)
            .println("Third line")
            .asString();

    assertThat(output).isEqualTo("  First line\n" + "\n" + "  Third line");
  }

  @Test
  void asString_when_refsPrinted_then_javaLangImportsDropped() {
    final String output =
        javaWriter().printRefs().ref("java.lang.Integer").ref("java.util.Optional").asString();

    assertThat(output).isEqualTo("import java.util.Optional;");
  }

  @Test
  void printSingleBlankLine_when_calledTwice_then_singleBlankLineAppended() {
    final String output =
        javaWriter()
            .println("hello")
            .printSingleBlankLine()
            .printSingleBlankLine()
            .println("HELLO")
            .asString();
    assertThat(output).isEqualTo("hello\n\nHELLO");
  }

  @Test
  void asString_when_hasMultipleNewLinesAndNoMultipleNewLinesSettings_then_singleNewLine() {
    final String output =
        javaWriter().println("Hello").println().println().println("World").asString();

    assertThat(output).isEqualTo("Hello\n" + "\n" + "World");
  }

  @Test
  void asString_when_hasMultipleNewLinesAndAllowMultipleNewLinesSettings_then_singleNewLine() {
    final String output =
        javaWriter(new WriterSettings(2, false))
            .println("Hello")
            .println()
            .println()
            .println("World")
            .asString();

    assertThat(output).isEqualTo("Hello\n" + "\n" + "\n" + "World");
  }

  @Test
  void
      asString_when_newLineBeforeAndAfterRefButNoRefsAndNoMultipleNewLinesSettings_then_singleNewLine() {
    final String output =
        javaWriter().println("Hello").println().printRefs().println().println("World").asString();

    assertThat(output).isEqualTo("Hello\n" + "\n" + "World");
  }

  @Test
  void
      asString_when_newLineBeforeAndAfterRefAndNoMultipleNewLinesSettings_then_newLineBeforeAndAfterRefs() {
    final String output =
        javaWriter()
            .println("Hello")
            .println()
            .printRefs()
            .println()
            .println("World")
            .ref("ref-123")
            .asString();

    assertThat(output).isEqualTo("Hello\n" + "\n" + "import ref-123;\n" + "\n" + "World");
  }

  @Test
  void resetToLastNonEmptyLine_when_called_then_followingPrintStatementsAddToLastNonEmptyLine() {
    final String output =
        javaWriter()
            .println("Hello")
            .println()
            .println("World")
            .println()
            .resetToLastNotEmptyLine()
            .println("!")
            .asString();

    assertThat(output).isEqualTo("Hello\n\nWorld!");
  }
}
