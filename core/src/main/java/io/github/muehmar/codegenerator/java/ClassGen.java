package io.github.muehmar.codegenerator.java;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.util.Strings;
import io.github.muehmar.codegenerator.writer.Writer;
import io.github.muehmar.pojobuilder.annotations.FieldBuilder;
import io.github.muehmar.pojobuilder.annotations.PojoBuilder;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@PojoBuilder
public class ClassGen<A, B> implements Generator<A, B> {
  private final ClassType type;
  private final Declaration declaration;
  private final Generator<A, B> packageGen;
  private final Generator<A, B> javaDocGen;
  private final PList<Generator<A, B>> annotationGens;
  private final BiFunction<A, B, JavaModifiers> modifiers;
  private final BiFunction<A, B, String> createClassName;
  private final BiFunction<A, B, Optional<String>> superClass;
  private final BiFunction<A, B, PList<String>> interfaces;
  private final PList<Generator<A, B>> content;

  @SuppressWarnings("java:S107")
  ClassGen(
      ClassType type,
      Declaration declaration,
      Generator<A, B> packageGen,
      Generator<A, B> javaDocGen,
      PList<Generator<A, B>> annotationGens,
      BiFunction<A, B, JavaModifiers> modifiers,
      BiFunction<A, B, String> createClassName,
      BiFunction<A, B, Optional<String>> superClass,
      BiFunction<A, B, PList<String>> interfaces,
      PList<Generator<A, B>> content) {
    this.type = type;
    this.declaration = declaration;
    this.packageGen = packageGen;
    this.javaDocGen = javaDocGen;
    this.annotationGens = annotationGens;
    this.modifiers = modifiers;
    this.createClassName = createClassName;
    this.superClass = superClass;
    this.interfaces = interfaces;
    this.content = content;
  }

  @Override
  public Writer generate(A data, B settings, Writer writer) {

    final Generator<A, B> contentGenerator =
        content.reduce(Generator::append).orElse((d, s, w) -> w);

    return packageGen()
        .append(this::refs)
        .append(javaDocGen)
        .append(annotationGens.reduce(Generator::append).orElse(Generator.emptyGen()))
        .append(this::classStart)
        .append(contentGenerator, 1)
        .append(this::classEnd)
        .generate(data, settings, writer);
  }

  private Generator<A, B> packageGen() {
    if (declaration.equals(Declaration.NESTED)) {
      return Generator.emptyGen();
    }

    return packageGen.appendNewLine();
  }

  private Writer refs(A data, B settings, Writer writer) {
    if (declaration.equals(Declaration.NESTED)) {
      return writer;
    }

    return writer.printRefs().println();
  }

  private Writer classStart(A data, B settings, Writer writer) {
    final String superClassStr =
        Strings.surroundIfNotEmpty(" extends ", superClass.apply(data, settings).orElse(""), "");

    final String interfaceInheritance = type.equals(ClassType.INTERFACE) ? "extends" : "implements";

    final String interfacesStr =
        Strings.surroundIfNotEmpty(
            " " + interfaceInheritance + " ", interfaces.apply(data, settings).mkString(", "), "");

    return writer.println(
        "%s%s %s%s%s {",
        modifiers.apply(data, settings).asStringTrailingWhitespace(),
        type.value,
        createClassName.apply(data, settings),
        superClassStr,
        interfacesStr);
  }

  private Writer classEnd(A data, B settings, Writer writer) {
    return writer.println("}");
  }

  @FieldBuilder(fieldName = "type")
  static class TypeBuilder {
    private TypeBuilder() {}

    static ClassType ifc() {
      return ClassType.INTERFACE;
    }

    static ClassType clazz() {
      return ClassType.CLASS;
    }

    static ClassType enum_() {
      return ClassType.ENUM;
    }
  }

  @FieldBuilder(fieldName = "declaration")
  static class DeclarationBuilder1 {
    private DeclarationBuilder1() {}

    static Declaration topLevel() {
      return Declaration.TOP_LEVEL;
    }

    static Declaration nested() {
      return Declaration.NESTED;
    }
  }

  @FieldBuilder(fieldName = "packageGen")
  static class PackageBuilder {
    private PackageBuilder() {}
  }

  @FieldBuilder(fieldName = "javaDocGen", disableDefaultMethods = true)
  static class JavaDocBuilder {
    private JavaDocBuilder() {}

    static <A, B> Generator<A, B> noJavaDoc() {
      return Generator.emptyGen();
    }

    static <A, B> Generator<A, B> javaDoc(Generator<A, B> javaDocGen) {
      return javaDocGen;
    }

    static <A, B> Generator<A, B> javaDoc(BiFunction<A, B, String> genJavaDoc) {
      return (data, settings, writer) -> writer.println(genJavaDoc.apply(data, settings));
    }

    static <A, B> Generator<A, B> javaDoc(Function<A, String> genJavaDoc) {
      return (data, settings, writer) -> writer.println(genJavaDoc.apply(data));
    }
  }

  @FieldBuilder(fieldName = "annotationGens", disableDefaultMethods = true)
  static class AnnotationGenBuilder {
    private AnnotationGenBuilder() {}

    static <A, B> PList<Generator<A, B>> noAnnotations() {
      return PList.empty();
    }

    static <A, B> PList<Generator<A, B>> singleAnnotation(Generator<A, B> annotation) {
      return PList.single(annotation);
    }

    static <A, B> PList<Generator<A, B>> singleAnnotation(BiFunction<A, B, String> annotation) {
      return PList.single((a, b, writer) -> writer.println(annotation.apply(a, b)));
    }

    static <A, B> PList<Generator<A, B>> singleAnnotation(Function<A, String> annotation) {
      return PList.single((a, b, writer) -> writer.println(annotation.apply(a)));
    }

    static <A, B> PList<Generator<A, B>> annotations(PList<Generator<A, B>> annotations) {
      return annotations;
    }
  }

  @FieldBuilder(fieldName = "modifiers")
  static class ModifierBuilder {
    private ModifierBuilder() {}

    static <A, B> BiFunction<A, B, JavaModifiers> noModifiers() {
      return (data, settings) -> JavaModifiers.empty();
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(JavaModifier m1) {
      return (data, settings) -> JavaModifiers.of(m1);
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifierList(
        BiFunction<A, B, PList<JavaModifier>> f) {
      return (data, settings) -> JavaModifiers.of(f.apply(data, settings));
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(JavaModifier m1, JavaModifier m2) {
      return (data, settings) -> JavaModifiers.of(m1, m2);
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(
        JavaModifier m1, JavaModifier m2, JavaModifier m3) {
      return (data, settings) -> JavaModifiers.of(m1, m2, m3);
    }

    static <A, B> BiFunction<A, B, JavaModifiers> modifiers(PList<JavaModifier> modifiers) {
      return (data, settings) -> JavaModifiers.of(modifiers);
    }
  }

  @FieldBuilder(fieldName = "createClassName", disableDefaultMethods = true)
  static class ClassNameBuilder {
    private ClassNameBuilder() {}

    static <A, B> BiFunction<A, B, String> className(BiFunction<A, B, Object> createClassName) {
      return (data, settings) -> createClassName.apply(data, settings).toString();
    }

    static <A, B> BiFunction<A, B, String> className(Function<A, Object> className) {
      return (data, settings) -> className.apply(data).toString();
    }

    static <A, B> BiFunction<A, B, String> className(String className) {
      return (data, settings) -> className;
    }
  }

  @FieldBuilder(fieldName = "superClass", disableDefaultMethods = true)
  static class SuperClassBuilder {
    private SuperClassBuilder() {}

    static <A, B> BiFunction<A, B, Optional<String>> noSuperClass() {
      return (data, settings) -> Optional.empty();
    }

    static <A, B> BiFunction<A, B, Optional<String>> superClass(
        BiFunction<A, B, Object> superClass) {
      return (data, settings) -> Optional.of(superClass.apply(data, settings).toString());
    }
  }

  @FieldBuilder(fieldName = "interfaces")
  static class InterfacesBuilder {
    private InterfacesBuilder() {}

    static <A, B> BiFunction<A, B, PList<String>> noInterfaces() {
      return (data, settings) -> PList.empty();
    }

    static <A, B> BiFunction<A, B, PList<String>> singleInterface(
        BiFunction<A, B, String> interfaceName) {
      return (data, settings) -> PList.of(interfaceName.apply(data, settings));
    }
  }

  @FieldBuilder(fieldName = "content")
  static class ContentBuilder {
    private ContentBuilder() {}

    static <A, B> PList<Generator<A, B>> noContent() {
      return PList.empty();
    }

    static <A, B> PList<Generator<A, B>> content(Generator<A, B> c1) {
      return PList.single(c1);
    }

    static <A, B> PList<Generator<A, B>> content(Generator<A, B> c1, Generator<A, B> c2) {
      return PList.of(c1, c2);
    }
  }

  public enum Declaration {
    TOP_LEVEL,
    NESTED
  }

  public enum ClassType {
    CLASS("class"),
    INTERFACE("interface"),
    ENUM("enum");

    private final String value;

    ClassType(String value) {
      this.value = value;
    }
  }
}
