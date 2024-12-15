package io.github.muehmar.codegenerator;

import static io.github.muehmar.codegenerator.Generator.constant;
import static io.github.muehmar.codegenerator.Generator.ofWriterFunction;
import static io.github.muehmar.codegenerator.TestData.booleanData;
import static io.github.muehmar.codegenerator.TestData.noData;
import static io.github.muehmar.codegenerator.TestSettings.noSettings;
import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.assertj.core.api.Assertions.assertThat;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.TestData.BooleanData;
import io.github.muehmar.codegenerator.TestData.ListData;
import io.github.muehmar.codegenerator.TestData.StringData;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class GeneratorTest {

  @Test
  void constant_when_created_then_correctOutput() {
    final Generator<Void, Void> gen = constant("Hello World!");
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("Hello World!");
  }

  @Test
  void constant_when_formatArguments_then_correctOutput() {
    final Generator<Void, Void> gen = constant("Hello %s!", "World");
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("Hello World!");
  }

  @Test
  void ofWriterFunction_when_created_then_correctOutput() {
    final Generator<Void, Void> gen = ofWriterFunction(w -> w.println("Hello World!"));
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("Hello World!");
  }

  @Test
  void appendGenerator_when_called_then_generatedCorrectAppended() {
    final Generator<Void, Void> genA = constant("genA");
    final Generator<Void, Void> genB = constant("genB");

    final Generator<Void, Void> gen = genA.append(genB);
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("genA\ngenB");
  }

  @Test
  void appendGeneratorWithTabs_when_called_then_generatedCorrectAppendedWithIndention() {
    final Generator<Void, Void> genA = constant("genA");
    final Generator<Void, Void> genB = constant("genB");

    final Generator<Void, Void> gen = genA.append(genB, 2);
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("genA\n    genB");
  }

  @Test
  void appendUnaryOperator_when_called_then_generatedCorrectAppend() {
    final Generator<Void, Void> genA = constant("genA");

    final Generator<Void, Void> gen = genA.append(w -> w.println("appended"));
    final Writer writer = gen.generate(noData(), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("genA\nappended");
  }

  @Test
  void appendMappingInput_when_called_then_generatedCorrectAppend() {
    final Generator<String, Void> genA = constant("genA");
    final Generator<Integer, Void> genB = (in, settings, writer) -> writer.println(in);

    final Generator<String, Void> gen = genA.append(genB, String::length);
    final Writer writer = gen.generate("Hello World!", noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("genA\n12");
  }

  @Test
  void appendMappingInputWithSettings_when_called_then_generatedCorrectAppend() {
    final Generator<String, Integer> genA = constant("genA");
    final Generator<Integer, Integer> genB = (in, settings, writer) -> writer.println(in);

    final Generator<String, Integer> gen =
        genA.append(genB, (in, settings) -> in.length() + settings);
    final Writer writer = gen.generate("Hello World!", 10, javaWriter());
    assertThat(writer.asString()).isEqualTo("genA\n22");
  }

  @Test
  void appendList_when_called_then_contentCreatedForEveryElementInTheList() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ListData::getList);
    final Writer writer = generator.generate(data, noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\nid\nusername\nnickname");
  }

  @Test
  void appendList_when_calledWithSeparator_then_separatorAddedBetween() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ListData::getList, ofWriterFunction(Writer::println));
    final Writer writer = generator.generate(data, noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\nid\n\nusername\n\nnickname");
  }

  @Test
  void appendList_when_emptyList_then_initialGenExecuted() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ignore -> PList.empty(), ofWriterFunction(Writer::println));
    final Writer writer = generator.generate(data, noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA");
  }

  @Test
  void appendOptional_when_nonEmpty_then_contentAdded() {
    final Generator<Optional<String>, Void> genA = constant("genA");
    final Generator<String, Void> genB = (data, settings, writer) -> writer.println("-> %s", data);

    final Generator<Optional<String>, Void> generator =
        genA.appendOptional(genB, Function.identity());
    final Writer writer = generator.generate(Optional.of("data"), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\n-> data");
  }

  @Test
  void appendOptional_when_empty_then_nothingAdded() {
    final Generator<Optional<String>, Void> genA = constant("genA");
    final Generator<String, Void> genB = (data, settings, writer) -> writer.println("-> %s", data);

    final Generator<Optional<String>, Void> generator =
        genA.appendOptional(genB, Function.identity());
    final Writer writer = generator.generate(Optional.empty(), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA");
  }

  @Test
  void appendConditionally_when_conditionEvaluatedToTrue_then_generatorAppended() {
    final Generator<BooleanData, Void> genA = constant("genA");
    final Generator<BooleanData, Void> genB = constant("genB");

    final Generator<BooleanData, Void> generator =
        genA.appendConditionally(genB, BooleanData::isFlag);
    final Writer writer = generator.generate(booleanData(true), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\ngenB");
  }

  @Test
  void appendConditionally_when_conditionEvaluatedToFalse_then_generatorNotAppended() {
    final Generator<BooleanData, Void> genA = constant("genA");
    final Generator<BooleanData, Void> genB = constant("genB");

    final Generator<BooleanData, Void> generator =
        genA.appendConditionally(genB, BooleanData::isFlag);
    final Writer writer = generator.generate(booleanData(false), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA");
  }

  @Test
  void filter_when_conditionIsTrue_then_generatorUnchanged() {
    final Generator<Integer, Integer> genA = constant("genA");

    final Generator<Integer, Integer> generator = genA.filter((i1, i2) -> i1 + i2 == 3);

    final Writer writer = generator.generate(1, 2, javaWriter());

    assertThat(writer.asString()).isEqualTo("genA");
  }

  @Test
  void filter_when_conditionIsFalse_then_emptyGeneratorReturned() {
    final Generator<Integer, Integer> genA = constant("genA");

    final Generator<Integer, Integer> generator = genA.filter((i1, i2) -> i1 + i2 == 3);

    final Writer writer = generator.generate(2, 2, javaWriter());

    assertThat(writer.asString()).isEqualTo("");
  }

  @Test
  void appendSingleBlankLine_when_called_then_outputHasNewLineAppended() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer =
        genA.appendSingleBlankLine()
            .appendSingleBlankLine()
            .generate(noData(), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\n");
  }

  @Test
  void appendNewLine_when_called_then_outputHasNewLineAppended() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer = genA.appendNewLine().generate(noData(), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("genA\n");
  }

  @Test
  void prependNewLine_when_called_then_outputHasNewLinePrepended() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer = genA.prependNewLine().generate(noData(), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("\ngenA");
  }

  @Test
  void int_when_called_then_indentedByGivenTabs() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer = genA.indent(2).generate(noData(), noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("    genA");
  }

  @Test
  void contraMap_when_newGenCalled_then_inputTransformedAccordingly() {
    final Generator<String, Void> genA = (s, ignore, w) -> w.println(s);

    final Generator<Integer, Void> generator = genA.contraMap(Integer::toHexString);

    final Writer writer = generator.generate(255, noSettings(), javaWriter());

    assertThat(writer.asString()).isEqualTo("ff");
  }
}
