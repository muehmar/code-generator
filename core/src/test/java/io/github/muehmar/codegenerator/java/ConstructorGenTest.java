package io.github.muehmar.codegenerator.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.writer.Writer;
import org.junit.jupiter.api.Test;

class ConstructorGenTest {
  @Test
  void generate_when_minimalGeneratorCreated_then_outputCorrect() {
    final ConstructorGen<PList<String>, Void> generator =
        ConstructorGenBuilder.<PList<String>, Void>create()
            .modifiers(JavaModifier.PUBLIC)
            .className(l -> l.apply(0))
            .arguments(l -> l.drop(1))
            .content("System.out.println(\"Hello World\");")
            .build();

    final PList<String> data = PList.of("Customer", "String a", "int b");

    final String output = generator.generate(data, null, Writer.createDefault()).asString();
    assertEquals(
        "public Customer(String a, int b) {\n" + "  System.out.println(\"Hello World\");\n" + "}",
        output);
  }
}
