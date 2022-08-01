package io.github.muehmar.codegenerator.java;

public class JavaGenerators {
  private JavaGenerators() {}

  public static <A, B> MethodGenBuilder.Builder0<A, B> methodGen() {
    return MethodGenBuilder.create();
  }

  public static <A, B> ConstructorGenBuilder.Builder0<A, B> constructorGen() {
    return ConstructorGenBuilder.create();
  }

  public static <A, B> ClassGenBuilder.Builder0<A, B> classGen() {
    return ClassGenBuilder.create();
  }
}
