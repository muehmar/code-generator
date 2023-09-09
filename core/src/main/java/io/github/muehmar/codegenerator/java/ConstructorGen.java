package io.github.muehmar.codegenerator.java;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.writer.Writer;
import io.github.muehmar.pojobuilder.annotations.FieldBuilder;
import io.github.muehmar.pojobuilder.annotations.PojoBuilder;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.Value;

@PojoBuilder
public class ConstructorGen<A, B> implements Generator<A, B> {
  private final BiFunction<A, B, JavaModifiers> createModifiers;
  private final BiFunction<A, B, String> createClassName;
  private final BiFunction<A, B, PList<Argument>> createArguments;
  private final Generator<A, B> contentGenerator;

  ConstructorGen(
      BiFunction<A, B, JavaModifiers> createModifiers,
      BiFunction<A, B, String> createClassName,
      BiFunction<A, B, PList<Argument>> createArguments,
      Generator<A, B> contentGenerator) {
    this.createModifiers = createModifiers;
    this.createClassName = createClassName;
    this.createArguments = createArguments;
    this.contentGenerator = contentGenerator;
  }

  @Override
  public Writer generate(A data, B settings, Writer writer) {
    return Generator.<A, B>ofWriterFunction(
            w -> {
              final String arguments =
                  createArguments
                      .apply(data, settings)
                      .map(arg -> String.format("%s %s", arg.type, arg.name))
                      .mkString(", ");
              final String className = createClassName.apply(data, settings);
              final JavaModifiers modifiers = createModifiers.apply(data, settings);
              return w.print(
                  "%s%s(%s) {", modifiers.asStringTrailingWhitespace(), className, arguments);
            })
        .append(contentGenerator, 1)
        .append(w -> w.println("}"))
        .generate(data, settings, writer);
  }

  @FieldBuilder(fieldName = "createModifiers", disableDefaultMethods = true)
  static class ModifiersGen {
    private ModifiersGen() {}

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers() {
      return (d, s) -> JavaModifiers.empty();
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(JavaModifier modifier) {
      return (d, s) -> JavaModifiers.of(modifier);
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(JavaModifier m1, JavaModifier m2) {
      return (d, s) -> JavaModifiers.of(m1, m2);
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(JavaModifiers javaModifiers) {
      return (d, s) -> javaModifiers;
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(
        BiFunction<A, B, JavaModifiers> modifiers) {
      return modifiers;
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(Function<A, JavaModifiers> modifiers) {
      return (d, s) -> modifiers.apply(d);
    }
  }

  @FieldBuilder(fieldName = "createClassName", disableDefaultMethods = true)
  static class ClassNameBuilder {
    private ClassNameBuilder() {}

    static <A, B> BiFunction<A, B, String> className(BiFunction<A, B, Object> createClassName) {
      return (data, settings) -> createClassName.apply(data, settings).toString();
    }

    static <A, B> BiFunction<A, B, String> className(Function<A, Object> createClassName) {
      return (data, settings) -> createClassName.apply(data).toString();
    }

    static <A, B> BiFunction<A, B, String> className(String className) {
      return (d, s) -> className;
    }
  }

  @FieldBuilder(fieldName = "createArguments", disableDefaultMethods = true)
  static class ArgumentsBuilder {
    private ArgumentsBuilder() {}

    static <A, B> BiFunction<A, B, PList<Argument>> arguments(
        BiFunction<A, B, PList<Argument>> createArguments) {
      return createArguments;
    }

    static <A, B> BiFunction<A, B, PList<Argument>> arguments(
        Function<A, PList<Argument>> createArguments) {
      return (d, s) -> createArguments.apply(d);
    }

    static <A, B> BiFunction<A, B, PList<Argument>> singleArgument(
        Function<A, Argument> createArgument) {
      return (d, s) -> PList.single(createArgument.apply(d));
    }

    static <A, B> BiFunction<A, B, PList<Argument>> singleArgument(Argument argument) {
      return (d, s) -> PList.single(argument);
    }

    static <A, B> BiFunction<A, B, PList<Argument>> noArguments() {
      return (d, s) -> PList.empty();
    }
  }

  @FieldBuilder(fieldName = "contentGenerator", disableDefaultMethods = true)
  static class ContentBuilder {
    private ContentBuilder() {}

    static <A, B> Generator<A, B> content(String content) {
      return (data, settings, writer) -> writer.println(content);
    }

    static <A, B> Generator<A, B> content(Generator<A, B> content) {
      return content;
    }

    static <A, B> Generator<A, B> content(UnaryOperator<Writer> content) {
      return (data, settings, writer) -> content.apply(writer);
    }

    static <A, B> Generator<A, B> memberAssignmentContent(
        Function<A, PList<Argument>> createArguments) {
      return memberAssignmentContent((data, settings) -> createArguments.apply(data));
    }

    static <A, B> Generator<A, B> memberAssignmentContent(
        BiFunction<A, B, PList<Argument>> createArguments) {
      return (data, settings, writer) ->
          createArguments
              .apply(data, settings)
              .map(arg -> String.format("this.%s = %s;", arg.name, arg.name))
              .foldLeft(writer, Writer::println);
    }

    static <A, B> Generator<A, B> noContent() {
      return (data, settings, writer) -> writer;
    }
  }

  @Value
  public static class Argument {
    String type;
    String name;

    public static Argument argument(String type, String name) {
      return new Argument(type, name);
    }

    public static Argument argument(Object type, String name) {
      return new Argument(type.toString(), name);
    }

public static Argument argument(String type, Object name) {
      return new Argument(type, name.toString());
    }

public static Argument argument(Object type, Object name) {
      return new Argument(type.toString(), name.toString());
    }
  }
}
