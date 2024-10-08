package io.github.muehmar.codegenerator.writer;

import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WriterTest {
  @Test
  void print_when_formatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = javaWriter().print("Format %s and %s!", "this", "that");
    assertEquals("Format this and that!", writer.asString());
  }

  @Test
  void tabAndPrint_when_tabAndFormatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = javaWriter().tab(1).print("Format %s and %s!", "this", "that");
    assertEquals("  Format this and that!", writer.asString());
  }

  @Test
  void print_when_calledMultipleTimes_then_everythingOnOneLine() {
    final Writer writer =
        javaWriter()
            .print("Format %s and %s!", "this", "that")
            .print(" And put")
            .print(" everything on the same")
            .print(" line.");
    assertEquals("Format this and that! And put everything on the same line.", writer.asString());
  }

  @Test
  void println_when_calledTwoTimes_then_twoLinesCreated() {
    final Writer writer = javaWriter().println("Line number %d", 1).println("Line number %d", 2);
    assertEquals("Line number 1\nLine number 2", writer.asString());
  }

  @Test
  void tabAndPrintln_when_tabCalledBeforePrintLn_then_tabResettedAfterPrintLnCalled() {
    final Writer writer = javaWriter().tab(2).println("First line").println("Second line");
    assertEquals("    First line\n" + "Second line", writer.asString());
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
    assertEquals(
        "First line\n"
            + "import Ref A;\n"
            + "import Ref B;\n"
            + "import Ref C;\n"
            + "Second line\n"
            + "Third line",
        writer.asString());
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
    assertEquals(
        "First line of main writer\n"
            + "Something with a newline\n"
            + "Line after writer A\n"
            + "Something without a newline\n"
            + "Line after writer B",
        writer.asString());
  }

  @Test
  void append_when_calledWithTabs_then_correctIndentionForAppendedContent() {
    final Writer writerA = javaWriter().println("Content writer A");

    final Writer writer =
        javaWriter()
            .println("First line of main writer")
            .append(2, writerA)
            .println("Some other line");
    assertEquals(
        "First line of main writer\n" + "    Content writer A\nSome other line", writer.asString());
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
    assertEquals(
        "First line of main writer\n"
            + "import Main writer ref;\n"
            + "import Writer A ref;\n"
            + "Something of writer A\n"
            + "Line after writer A",
        writer.asString());
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

    assertEquals("  First line\n" + "\n" + "  Third line", output);
  }

  @Test
  void asString_when_refsPrinted_then_javaLangImportsDropped() {
    final String output =
        javaWriter().printRefs().ref("java.lang.Integer").ref("java.util.Optional").asString();

    assertEquals("import java.util.Optional;", output);
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
    assertEquals("hello\n\nHELLO", output);
  }

  @Test
  void asString_when_hasMultipleNewLinesAndNoMultipleNewLinesSettings_then_singleNewLine() {
    final String output =
        javaWriter().println("Hello").println().println().println("World").asString();

    assertEquals("Hello\n" + "\n" + "World", output);
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

    assertEquals("Hello\n" + "\n" + "\n" + "World", output);
  }

  @Test
  void
      asString_when_newLineBeforeAndAfterRefButNoRefsAndNoMultipleNewLinesSettings_then_singleNewLine() {
    final String output =
        javaWriter().println("Hello").println().printRefs().println().println("World").asString();

    assertEquals("Hello\n" + "\n" + "World", output);
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

    assertEquals("Hello\n" + "\n" + "import ref-123;\n" + "\n" + "World", output);
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

    assertEquals("Hello\n\nWorld!", output);
  }
}
