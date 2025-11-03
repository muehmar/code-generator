package io.github.muehmar.codegenerator.java;

public class JavaGenerators {
  private JavaGenerators() {}

  public static <A, B> MethodGenBuilder.BuilderStages.Builder0<A, B> methodGen() {
    return MethodGenBuilder.create();
  }

  public static <A, B> ConstructorGenBuilder.BuilderStages.Builder0<A, B> constructorGen() {
    return ConstructorGenBuilder.create();
  }

  public static <A, B> ClassGenBuilder.BuilderStages.Builder0<A, B> classGen() {
    return ClassGenBuilder.create();
  }
}
