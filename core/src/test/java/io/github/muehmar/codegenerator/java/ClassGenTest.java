package io.github.muehmar.codegenerator.java;

import static io.github.muehmar.codegenerator.TestSettings.noSettings;
import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.assertj.core.api.Assertions.assertThat;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.TestData;
import io.github.muehmar.codegenerator.TestData.ListData;
import io.github.muehmar.codegenerator.TestData.StringData;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClassGenTest {

  public static final Generator<StringData, Void> PACKAGE_GEN =
      Generator.constant("package io.github.muehmar;");

  @Test
  void generate_when_simplePojoAndSingleContent_then_correctGeneratedString() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.constant("Content"))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "public class HelloWorld {\n"
                + "  Content\n"
                + "}");
  }

  @Test
  void generate_when_javaDoc_then_correctGeneratedString() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .javaDoc((data, settings) -> String.format("/** %s */", data.getText()))
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.constant("Content"))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "/** HelloWorld */\n"
                + "public class HelloWorld {\n"
                + "  Content\n"
                + "}");
  }

  @Test
  void generate_when_interface_then_correctOutput() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .ifc()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.println("Content")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "public interface HelloWorld {\n"
                + "  Content\n"
                + "}");
  }

  @Test
  void generate_when_enum_then_correctOutput() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .enum_()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.println("Content")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "public enum HelloWorld {\n"
                + "  Content\n"
                + "}");
  }

  @Test
  void generate_when_interfaceInheritsInterface_then_correctInheritance() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .ifc()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .singleInterface((data, settings) -> "World")
            .content(Generator.ofWriterFunction(w -> w.println("Content")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "public interface HelloWorld extends World {\n"
                + "  Content\n"
                + "}");
  }

  @Test
  void generate_when_refAddedInContent_then_refPrinted() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .topLevel()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.ref("java.util.Optional")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "package io.github.muehmar;\n"
                + "\n"
                + "import java.util.Optional;\n"
                + "\n"
                + "public class HelloWorld {\n"
                + "}");
  }

  @Test
  void generate_when_nestedClass_then_noRefsAndPackagePrinted() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .nested()
            .packageGen(Generator.emptyGen())
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.ref("import java.util.Optional;")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("public class HelloWorld {\n" + "}");
  }

  @ParameterizedTest
  @MethodSource("publicAndFinalModifierUnordered")
  void generate_when_privateAndFinalModifierUnordered_then_correctOutputWithOrderedModifiers(
      PList<JavaModifier> modifiers) {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .nested()
            .packageGen(PACKAGE_GEN)
            .noJavaDoc()
            .noAnnotations()
            .modifiers(modifiers)
            .className((data, s) -> data.getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.ref("import java.util.Optional;")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("public final class HelloWorld {\n" + "}");
  }

  @Test
  void generate_when_hasSuperClass_then_correctOutput() {
    final ClassGen<StringData, Void> generator =
        ClassGenBuilder.<StringData, Void>create()
            .clazz()
            .nested()
            .packageGen(Generator.emptyGen())
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((data, s) -> data.getText())
            .superClass((p, s) -> "Superclass")
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.ref("import java.util.Optional;")))
            .build();

    final Writer writer =
        generator.generate(new StringData("HelloWorld"), noSettings(), javaWriter());
    assertThat(writer.asString()).isEqualTo("public class HelloWorld extends Superclass {\n" + "}");
  }

  @Test
  void generate_when_hasSuperClassAndInterfaces_then_correctOutput() {
    final ClassGen<ListData<StringData>, Void> generator =
        ClassGenBuilder.<ListData<StringData>, Void>create()
            .clazz()
            .nested()
            .packageGen(Generator.emptyGen())
            .noJavaDoc()
            .noAnnotations()
            .modifiers(JavaModifier.PUBLIC)
            .className((p, s) -> p.getList().apply(0).getText())
            .superClass((p, s) -> "Superclass")
            .singleInterface((p, s) -> p.getList().apply(1).getText())
            .content(Generator.ofWriterFunction(w -> w.ref("import java.util.Optional;")))
            .build();

    final Writer writer =
        generator.generate(TestData.stringListData("Hello", "World"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo("public class Hello extends Superclass implements World {\n" + "}");
  }

  @Test
  void generate_when_annotations_then_correctOutput() {
    final ClassGen<ListData<StringData>, Void> generator =
        ClassGenBuilder.<ListData<StringData>, Void>create()
            .clazz()
            .nested()
            .packageGen(Generator.emptyGen())
            .noJavaDoc()
            .annotations(
                PList.of(
                    (d, s, w) -> w.println("@%s", d.getList().apply(0).getText()),
                    (d, s, w) -> w.println("@%s", d.getList().apply(1).getText())))
            .modifiers(JavaModifier.PUBLIC)
            .className((p, s) -> p.getList().apply(0).getText())
            .noSuperClass()
            .noInterfaces()
            .content(Generator.ofWriterFunction(w -> w.ref("import java.util.Optional;")))
            .build();

    final Writer writer =
        generator.generate(TestData.stringListData("Hello", "World"), noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo("@Hello\n" + "@World\n" + "public class Hello {\n" + "}");
  }

  private static Stream<Arguments> publicAndFinalModifierUnordered() {
    return Stream.of(
        Arguments.of(PList.of(JavaModifier.FINAL, JavaModifier.PUBLIC)),
        Arguments.of(PList.of(JavaModifier.PUBLIC, JavaModifier.FINAL)));
  }
}
