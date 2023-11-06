package io.github.muehmar.codegenerator.java;

import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.util.Strings;
import io.github.muehmar.codegenerator.writer.Writer;
import io.github.muehmar.pojobuilder.annotations.FieldBuilder;
import io.github.muehmar.pojobuilder.annotations.OptionalDetection;
import io.github.muehmar.pojobuilder.annotations.PojoBuilder;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.Value;

@PojoBuilder(optionalDetection = OptionalDetection.NONE)
public class MethodGen<A, B> implements Generator<A, B> {
  private final BiFunction<A, B, JavaModifiers> createModifiers;
  private final BiFunction<A, B, PList<String>> createGenericTypeParameters;
  private final Generator<A, B> createReturnType;
  private final BiFunction<A, B, String> createMethodName;
  private final BiFunction<A, B, PList<Argument>> createArguments;
  private final Generator<A, B> createThrownExceptions;
  private final Optional<Generator<A, B>> contentGenerator;

  MethodGen(
      BiFunction<A, B, JavaModifiers> createModifiers,
      BiFunction<A, B, PList<String>> createGenericTypeParameters,
      Generator<A, B> createReturnType,
      BiFunction<A, B, String> createMethodName,
      BiFunction<A, B, PList<Argument>> createArguments,
      Generator<A, B> createThrownExceptions,
      Optional<Generator<A, B>> contentGenerator) {
    this.createModifiers = createModifiers;
    this.createGenericTypeParameters = createGenericTypeParameters;
    this.createReturnType = createReturnType;
    this.createMethodName = createMethodName;
    this.createArguments = createArguments;
    this.createThrownExceptions = createThrownExceptions;
    this.contentGenerator = contentGenerator;
  }

  @Override
  public Writer generate(A data, B settings, Writer writer) {
    return Generator.<A, B>ofWriterFunction(
            w -> {
              final JavaModifiers modifiers = createModifiers.apply(data, settings);
              final String genericTypeParameters =
                  Strings.surroundIfNotEmpty(
                      "<", createGenericTypeParameters.apply(data, settings).mkString(", "), "> ");
              final Writer returnTypeWriter =
                  createReturnType.generate(data, settings, javaWriter());
              final String methodName = createMethodName.apply(data, settings);
              final String arguments =
                  createArguments
                      .apply(data, settings)
                      .map(arg -> String.format("%s %s", arg.type, arg.name))
                      .mkString(", ");
              final Writer exceptionsWriter =
                  createThrownExceptions.generate(data, settings, javaWriter());
              final String exceptions = exceptionsWriter.asString();
              final String throwsDeclaration =
                  Strings.surroundIfNotEmpty(" throws ", exceptions.trim(), "");
              final String openingBracket = contentGenerator.isPresent() ? " {" : ";";
              return w.print(
                      "%s%s%s %s(%s)%s%s",
                      modifiers.asStringTrailingWhitespace(),
                      genericTypeParameters,
                      returnTypeWriter.asString(),
                      methodName,
                      arguments,
                      throwsDeclaration,
                      openingBracket)
                  .refs(returnTypeWriter.getRefs())
                  .refs(exceptionsWriter.getRefs());
            })
        .append(contentGenerator.orElse(Generator.emptyGen()), 1)
        .append(w -> contentGenerator.isPresent() ? w.println("}") : w)
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

  @FieldBuilder(fieldName = "createGenericTypeParameters", disableDefaultMethods = true)
  static class GenericTypeParametersBuilder {
    private GenericTypeParametersBuilder() {}

    static <A, B> BiFunction<A, B, PList<String>> singleGenericType(Function<A, Object> type) {
      return (data, settings) -> PList.single(type.apply(data)).map(Object::toString);
    }

    static <A, B> BiFunction<A, B, PList<String>> genericTypes(String t1) {
      return (d, s) -> PList.single(t1);
    }

    static <A, B> BiFunction<A, B, PList<String>> genericTypes(String t1, String t2) {
      return (d, s) -> PList.of(t1, t2);
    }

    static <A, B> BiFunction<A, B, PList<String>> genericTypes(Function<A, PList<String>> types) {
      return (d, s) -> types.apply(d);
    }

    static <A, B> BiFunction<A, B, PList<String>> noGenericTypes() {
      return (d, s) -> PList.empty();
    }
  }

  @FieldBuilder(fieldName = "createReturnType", disableDefaultMethods = true)
  static class ReturnTypeBuilder {
    private ReturnTypeBuilder() {}

    static <A, B> Generator<A, B> returnType(Generator<A, B> gen) {
      return gen;
    }

    static <A, B> Generator<A, B> returnType(Function<A, Object> createReturnType) {
      return (d, s, w) -> w.println(createReturnType.apply(d).toString());
    }

    static <A, B> Generator<A, B> returnType(String returnType) {
      return (d, s, w) -> w.println(returnType);
    }
  }

  @FieldBuilder(fieldName = "createMethodName", disableDefaultMethods = true)
  static class MethodNameBuilder {
    private MethodNameBuilder() {}

    static <A, B> BiFunction<A, B, String> methodName(BiFunction<A, B, String> methodName) {
      return methodName;
    }

    static <A, B> BiFunction<A, B, String> methodName(Function<A, String> methodName) {
      return (data, settings) -> methodName.apply(data);
    }

    static <A, B> BiFunction<A, B, String> methodName(String methodName) {
      return (d, s) -> methodName;
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
      return (data, settings) -> createArguments.apply(data);
    }

    static <A, B> BiFunction<A, B, PList<Argument>> singleArgument(
        Function<A, Argument> createArgument) {
      return (d, s) -> PList.single(createArgument.apply(d));
    }

    static <A, B> BiFunction<A, B, PList<Argument>> noArguments() {
      return (d, s) -> PList.empty();
    }
  }

  @FieldBuilder(fieldName = "createThrownExceptions", disableDefaultMethods = true)
  static class ThrowsBuilder {
    private ThrowsBuilder() {}

    static <A, B> Generator<A, B> throwsExceptions(Generator<A, B> createThrows) {
      return createThrows;
    }

    static <A, B> Generator<A, B> throwsExceptions(BiFunction<A, B, PList<?>> createThrows) {
      return (d, s, w) -> w.println(createThrows.apply(d, s).map(Object::toString).mkString(", "));
    }

    static <A, B> Generator<A, B> throwsExceptions(Function<A, PList<?>> createThrows) {
      return throwsExceptions((d, s) -> createThrows.apply(d));
    }

    static <A, B> Generator<A, B> throwsSingleException(Function<A, ?> createThrows) {
      return throwsExceptions((d, s) -> PList.single(createThrows.apply(d)));
    }

    static <A, B> Generator<A, B> doesNotThrow() {
      return Generator.emptyGen();
    }
  }

  @FieldBuilder(fieldName = "contentGenerator", disableDefaultMethods = true)
  static class ContentBuilder {
    private ContentBuilder() {}

    static <A, B> Optional<Generator<A, B>> content(String content) {
      return Optional.of((data, settings, writer) -> writer.println(content));
    }

    static <A, B> Optional<Generator<A, B>> content(Function<A, String> content) {
      return Optional.of((data, settings, writer) -> writer.println(content.apply(data)));
    }

    static <A, B> Optional<Generator<A, B>> noBody() {
      return Optional.empty();
    }

    static <A, B> Optional<Generator<A, B>> contentWriter(UnaryOperator<Writer> content) {
      return Optional.of((data, settings, writer) -> content.apply(writer));
    }

    static <A, B> Optional<Generator<A, B>> content(Generator<A, B> content) {
      return Optional.of(content);
    }
  }

  @Value
  public static class Argument {
    String type;
    String name;

    public static Argument argument(String type, String name) {
      return new Argument(type, name);
    }

    public static Argument argument(String type, Object name) {
      return new Argument(type, name.toString());
    }

    public static Argument argument(Object type, String name) {
      return new Argument(type.toString(), name);
    }

    public static Argument argument(Object type, Object name) {
      return new Argument(type.toString(), name.toString());
    }
  }
}
