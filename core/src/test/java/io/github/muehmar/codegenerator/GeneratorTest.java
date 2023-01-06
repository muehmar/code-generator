package io.github.muehmar.codegenerator;

import static io.github.muehmar.codegenerator.Generator.constant;
import static io.github.muehmar.codegenerator.Generator.ofWriterFunction;
import static io.github.muehmar.codegenerator.TestData.booleanData;
import static io.github.muehmar.codegenerator.TestData.noData;
import static io.github.muehmar.codegenerator.TestSettings.noSettings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.TestData.BooleanData;
import io.github.muehmar.codegenerator.TestData.ListData;
import io.github.muehmar.codegenerator.TestData.StringData;
import io.github.muehmar.codegenerator.writer.Writer;
import org.junit.jupiter.api.Test;

class GeneratorTest {

  @Test
  void constant_when_created_then_correctOutput() {
    final Generator<Void, Void> gen = constant("Hello World!");
    final Writer writer = gen.generate(noData(), noSettings(), Writer.createDefault());
    assertEquals("Hello World!", writer.asString());
  }

  @Test
  void ofWriterFunction_when_created_then_correctOutput() {
    final Generator<Void, Void> gen = ofWriterFunction(w -> w.println("Hello World!"));
    final Writer writer = gen.generate(noData(), noSettings(), Writer.createDefault());
    assertEquals("Hello World!", writer.asString());
  }

  @Test
  void appendGenerator_when_called_then_generatedCorrectAppended() {
    final Generator<Void, Void> genA = constant("genA");
    final Generator<Void, Void> genB = constant("genB");

    final Generator<Void, Void> gen = genA.append(genB);
    final Writer writer = gen.generate(noData(), noSettings(), Writer.createDefault());
    assertEquals("genA\ngenB", writer.asString());
  }

  @Test
  void appendGeneratorWithTabs_when_called_then_generatedCorrectAppendedWithIndention() {
    final Generator<Void, Void> genA = constant("genA");
    final Generator<Void, Void> genB = constant("genB");

    final Generator<Void, Void> gen = genA.append(genB, 2);
    final Writer writer = gen.generate(noData(), noSettings(), Writer.createDefault());
    assertEquals("genA\n    genB", writer.asString());
  }

  @Test
  void appendUnaryOperator_when_called_then_generatedCorrectAppend() {
    final Generator<Void, Void> genA = constant("genA");

    final Generator<Void, Void> gen = genA.append(w -> w.println("appended"));
    final Writer writer = gen.generate(noData(), noSettings(), Writer.createDefault());
    assertEquals("genA\nappended", writer.asString());
  }

  @Test
  void appendList_when_called_then_contentCreatedForEveryElementInTheList() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ListData::getList);
    final Writer writer = generator.generate(data, noSettings(), Writer.createDefault());

    assertEquals("genA\nid\nusername\nnickname", writer.asString());
  }

  @Test
  void appendList_when_calledWithSeparator_then_separatorAddedBetween() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ListData::getList, ofWriterFunction(Writer::println));
    final Writer writer = generator.generate(data, noSettings(), Writer.createDefault());

    assertEquals("genA\nid\n\nusername\n\nnickname", writer.asString());
  }

  @Test
  void appendList_when_emptyList_then_initialGenExecuted() {
    final Generator<ListData<StringData>, Void> genA = constant("genA");
    final Generator<StringData, Void> fieldGen =
        (field, settings, writer) -> writer.println("%s", field.getText());

    final ListData<StringData> data = TestData.stringListData("id", "username", "nickname");

    final Generator<ListData<StringData>, Void> generator =
        genA.appendList(fieldGen, ignore -> PList.empty(), ofWriterFunction(Writer::println));
    final Writer writer = generator.generate(data, noSettings(), Writer.createDefault());

    assertEquals("genA", writer.asString());
  }

  @Test
  void appendConditionally_when_conditionEvaluatedToTrue_then_generatorAppended() {
    final Generator<BooleanData, Void> genA = constant("genA");
    final Generator<BooleanData, Void> genB = constant("genB");

    final Generator<BooleanData, Void> generator =
        genA.appendConditionally(BooleanData::isFlag, genB);
    final Writer writer =
        generator.generate(booleanData(true), noSettings(), Writer.createDefault());

    assertEquals("genA\ngenB", writer.asString());
  }

  @Test
  void appendConditionally_when_conditionEvaluatedToFalse_then_generatorNotAppended() {
    final Generator<BooleanData, Void> genA = constant("genA");
    final Generator<BooleanData, Void> genB = constant("genB");

    final Generator<BooleanData, Void> generator =
        genA.appendConditionally(BooleanData::isFlag, genB);
    final Writer writer =
        generator.generate(booleanData(false), noSettings(), Writer.createDefault());

    assertEquals("genA", writer.asString());
  }

  @Test
  void filter_when_conditionIsTrue_then_generatorUnchanged() {
    final Generator<Integer, Integer> genA = constant("genA");

    final Generator<Integer, Integer> generator = genA.filter((i1, i2) -> i1 + i2 == 3);

    final Writer writer = generator.generate(1, 2, Writer.createDefault());

    assertEquals("genA", writer.asString());
  }

  @Test
  void filter_when_conditionIsFalse_then_emptyGeneratorReturned() {
    final Generator<Integer, Integer> genA = constant("genA");

    final Generator<Integer, Integer> generator = genA.filter((i1, i2) -> i1 + i2 == 3);

    final Writer writer = generator.generate(2, 2, Writer.createDefault());

    assertEquals("", writer.asString());
  }

  @Test
  void appendNewLine_when_called_then_outputHasNewLineAppended() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer =
        genA.appendNewLine().generate(noData(), noSettings(), Writer.createDefault());

    assertEquals("genA\n", writer.asString());
  }

  @Test
  void prependNewLine_when_called_then_outputHasNewLinePrepended() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer =
        genA.prependNewLine().generate(noData(), noSettings(), Writer.createDefault());

    assertEquals("\ngenA", writer.asString());
  }

  @Test
  void int_when_called_then_indentedByGivenTabs() {
    final Generator<Void, Void> genA = constant("genA");

    final Writer writer = genA.indent(2).generate(noData(), noSettings(), Writer.createDefault());

    assertEquals("    genA", writer.asString());
  }

  @Test
  void contraMap_when_newGenCalled_then_inputTransformedAccordingly() {
    final Generator<String, Void> genA = (s, ignore, w) -> w.println(s);

    final Generator<Integer, Void> generator = genA.contraMap(Integer::toHexString);

    final Writer writer = generator.generate(255, noSettings(), Writer.createDefault());

    assertEquals("ff", writer.asString());
  }
}
