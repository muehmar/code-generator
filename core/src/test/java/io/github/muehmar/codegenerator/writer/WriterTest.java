package io.github.muehmar.codegenerator.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WriterTest {
  @Test
  void print_when_formatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = Writer.createDefault().print("Format %s and %s!", "this", "that");
    assertEquals("Format this and that!", writer.asString());
  }

  @Test
  void tabAndPrint_when_tabAndFormatStringWithArgs_then_formattedCorrectly() {
    final Writer writer = Writer.createDefault().tab(1).print("Format %s and %s!", "this", "that");
    assertEquals("  Format this and that!", writer.asString());
  }

  @Test
  void print_when_calledMultipleTimes_then_everythingOnOneLine() {
    final Writer writer =
        Writer.createDefault()
            .print("Format %s and %s!", "this", "that")
            .print(" And put")
            .print(" everything on the same")
            .print(" line.");
    assertEquals("Format this and that! And put everything on the same line.", writer.asString());
  }

  @Test
  void println_when_calledTwoTimes_then_twoLinesCreated() {
    final Writer writer =
        Writer.createDefault().println("Line number %d", 1).println("Line number %d", 2);
    assertEquals("Line number 1\nLine number 2", writer.asString());
  }

  @Test
  void tabAndPrintln_when_tabCalledBeforePrintLn_then_tabResettedAfterPrintLnCalled() {
    final Writer writer =
        Writer.createDefault().tab(2).println("First line").println("Second line");
    assertEquals("    First line\n" + "Second line", writer.asString());
  }

  @Test
  void refAndPrintRefs_when_bothCalled_then_refsPrintedAndOrderedCorrectly() {
    final Writer writer =
        Writer.createDefault()
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
    final Writer writerA = Writer.createDefault().println("Something with a newline");
    final Writer writerB = Writer.createDefault().print("Something without a newline");

    final Writer writer =
        Writer.createDefault()
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
    final Writer writerA = Writer.createDefault().println("Content writer A");

    final Writer writer =
        Writer.createDefault()
            .println("First line of main writer")
            .append(2, writerA)
            .println("Some other line");
    assertEquals(
        "First line of main writer\n" + "    Content writer A\nSome other line", writer.asString());
  }

  @Test
  void append_when_writersPrintAndAddRefs_then_allRefsPrinted() {
    final Writer writerA =
        Writer.createDefault().println("Something of writer A").ref("Writer A ref");

    final Writer writer =
        Writer.createDefault()
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
        Writer.createDefault()
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
        Writer.createDefault()
            .printRefs()
            .ref("java.lang.Integer")
            .ref("java.util.Optional")
            .asString();

    assertEquals("import java.util.Optional;", output);
  }
}
