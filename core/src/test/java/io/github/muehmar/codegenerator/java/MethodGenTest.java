package io.github.muehmar.codegenerator.java;

import static io.github.muehmar.codegenerator.TestSettings.noSettings;
import static io.github.muehmar.codegenerator.java.JavaModifier.FINAL;
import static io.github.muehmar.codegenerator.java.JavaModifier.PUBLIC;
import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.assertj.core.api.Assertions.assertThat;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.writer.Writer;
import lombok.Value;
import org.junit.jupiter.api.Test;

class MethodGenTest {
  @Test
  void generate_when_minimalGeneratorCreated_then_outputCorrect() {
    final MethodGen<Data, Void> generator =
        MethodGenBuilder.<Data, Void>create()
            .modifiers(PUBLIC, FINAL)
            .noGenericTypes()
            .returnType(Data::getReturnType)
            .methodName(Data::getMethodName)
            .arguments(Data::getArguments)
            .doesNotThrow()
            .contentWriter(w -> w.println("System.out.println(\"Hello World\");"))
            .build();

    final Data data =
        new Data(
            "void",
            "getXY",
            PList.of(new MethodGen.Argument("String", "a"), new MethodGen.Argument("int", "b")));

    final String output = generator.generate(data, noSettings(), javaWriter()).asString();
    assertThat(output)
        .isEqualTo(
            "public final void getXY(String a, int b) {\n"
                + "  System.out.println(\"Hello World\");\n"
                + "}");
  }

  @Test
  void generate_when_methodWithGenerics_then_outputCorrect() {
    final MethodGen<String, Void> generator =
        MethodGenBuilder.<String, Void>create()
            .modifiers(PUBLIC, FINAL)
            .genericTypes("T, S")
            .returnType("T")
            .methodName("doSomething")
            .singleArgument(ignore -> new MethodGen.Argument("S", "s"))
            .throwsExceptions(ignore -> PList.of("IllegalStateException", "IOException"))
            .contentWriter(w -> w.println("return s.getT();"))
            .build();

    final String output = generator.generate("data", noSettings(), javaWriter()).asString();
    assertThat(output)
        .isEqualTo(
            "public final <T, S> T doSomething(S s) throws IllegalStateException, IOException {\n"
                + "  return s.getT();\n"
                + "}");
  }

  @Test
  void generate_when_methodWithExceptionsGenerator_then_outputCorrect() {
    final MethodGen<String, Void> generator =
        MethodGenBuilder.<String, Void>create()
            .modifiers(PUBLIC, FINAL)
            .genericTypes("T, S")
            .returnType("T")
            .methodName("doSomething")
            .singleArgument(ignore -> new MethodGen.Argument("S", "s"))
            .throwsExceptions(
                (d, s, w) -> w.println("CustomException").ref("io.github.muehmar.CustomException"))
            .contentWriter(w -> w.println("return s.getT();"))
            .build();

    final Writer writer = generator.generate("data", noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo(
            "public final <T, S> T doSomething(S s) throws CustomException {\n"
                + "  return s.getT();\n"
                + "}");
    assertThat(writer.getRefs()).isEqualTo(PList.single("io.github.muehmar.CustomException"));
  }

  @Test
  void generate_when_generatorForReturnType_then_outputAndRefsCorrect() {
    final MethodGen<String, Void> generator =
        MethodGenBuilder.<String, Void>create()
            .modifiers(PUBLIC, FINAL)
            .noGenericTypes()
            .returnType((d, s, w) -> w.println("returnSomething").ref("somethingRef"))
            .methodName("doSomething")
            .noArguments()
            .doesNotThrow()
            .contentWriter(w -> w.println("return xyz;"))
            .build();

    final Writer writer = generator.generate("data", noSettings(), javaWriter());
    assertThat(writer.asString())
        .isEqualTo("public final returnSomething doSomething() {\n" + "  return xyz;\n" + "}");
    assertThat(writer.getRefs().exists("somethingRef"::equals)).isTrue();
  }

  @Value
  private static class Data {
    String returnType;
    String methodName;
    PList<MethodGen.Argument> arguments;
  }
}
