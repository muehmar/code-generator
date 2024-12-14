package io.github.muehmar.codegenerator.java;

import static io.github.muehmar.codegenerator.TestSettings.noSettings;
import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.muehmar.codegenerator.Generator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class JavaDocGeneratorTest {

  @Test
  void javaDoc_when_javaDocWithLineBreaksButAlsoLongLines_then_keepLineBreaksAndAutoNewLine() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();
    final String input =
        "This is some javadoc\n"
            + " 1. Line one\n"
            + " 2. Line two\n"
            + " 3. This is a very long line! This is a very long line! This is a very long line! "
            + "This is a very long line! This is a very long line! This is a very long line! "
            + "This is a very long line! This is a very long line! This is a very long line!";

    final String output = generator.generate(input, noSettings(), javaWriter()).asString();

    assertThat(output)
        .isEqualTo(
            "/**\n"
                + " * This is some javadoc<br>\n"
                + " *  1. Line one<br>\n"
                + " *  2. Line two<br>\n"
                + " *  3. This is a very long line! This is a very long line! This is a very long\n"
                + " * line! This is a very long line! This is a very long line! This is a very long\n"
                + " * line! This is a very long line! This is a very long line! This is a very long\n"
                + " * line!\n"
                + " */");
  }

  @Test
  void javaDoc_when_singleWordIsLongerThanMaxLength_then_longWordOnSingleLine() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();
    final String input =
        "Small Word ThisIsAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongWord Small Word";

    final String output = generator.generate(input, noSettings(), javaWriter()).asString();

    assertThat(output)
        .isEqualTo(
            "/**\n"
                + " * Small Word\n"
                + " * ThisIsAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongWord\n"
                + " * Small Word\n"
                + " */");
  }

  @Test
  void javaDoc_when_singleShortLine_then_singleLineJavaDoc() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();
    final String input = "Single Line Java Doc";

    final String output = generator.generate(input, noSettings(), javaWriter()).asString();

    assertThat(output).isEqualTo("/**\n" + " * Single Line Java Doc\n" + " */");
  }

  @Test
  void javaDoc_when_lineWith18CharactersAndEndsWithSpace_then_lastSpaceDiscarded() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();
    final String input =
        "This line contains 81 characters (maxlength+1) and the last character is a space ";

    assertThat(input.length()).isEqualTo(81);

    final String output = generator.generate(input, noSettings(), javaWriter()).asString();

    assertThat(output)
        .isEqualTo(
            "/**\n"
                + " * This line contains 81 characters (maxlength+1) and the last character is a space\n"
                + " */");
  }

  @Test
  void javaDoc_when_singleLongWord_then_singleLineJavaDoc() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();
    final char[] characters = new char[80];
    Arrays.fill(characters, 'o');
    final String input = "L" + new String(characters) + "ongWord";

    final String output = generator.generate(input, noSettings(), javaWriter()).asString();

    assertThat(output)
        .isEqualTo(
            "/**\n"
                + " * LooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongWord\n"
                + " */");
  }

  @Test
  void javaDoc_when_doubleLineBreak_then_keepLineBreaksAndAddBreakTag() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();

    final String output =
        generator
            .generate(
                "This is a line followed by a double line break\n\nThe line that follows",
                noSettings(),
                javaWriter())
            .asString();

    assertThat(output)
        .isEqualTo(
            "/**\n"
                + " * This is a line followed by a double line break<br>\n"
                + " * <br>\n"
                + " * The line that follows\n"
                + " */");
  }

  @Test
  void javaDoc_when_noJavaDocText_then_notOutput() {
    final Generator<String, Void> generator = JavaDocGenerator.javaDoc();

    final String output = generator.generate("", noSettings(), javaWriter()).asString();

    assertThat(output).isEqualTo("");
  }
}
