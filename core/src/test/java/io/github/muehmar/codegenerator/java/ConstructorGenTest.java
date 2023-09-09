package io.github.muehmar.codegenerator.java;

import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.bluecare.commons.data.PList;
import lombok.Value;
import org.junit.jupiter.api.Test;

class ConstructorGenTest {
  @Test
  void generate_when_minimalGeneratorCreated_then_outputCorrect() {
    final ConstructorGen<Data, Void> generator =
        ConstructorGenBuilder.<Data, Void>create()
            .modifiers(JavaModifier.PUBLIC)
            .className(Data::getClassname)
            .arguments(Data::getArguments)
            .content("System.out.println(\"Hello World\");")
            .build();

    final Data data =
        new Data(
            "Customer",
            PList.of(
                new ConstructorGen.Argument("String", "a"),
                new ConstructorGen.Argument("int", "b")));

    final String output = generator.generate(data, null, javaWriter()).asString();
    assertEquals(
        "public Customer(String a, int b) {\n" + "  System.out.println(\"Hello World\");\n" + "}",
        output);
  }

  @Value
  private static class Data {
    String classname;
    PList<ConstructorGen.Argument> arguments;
  }
}
