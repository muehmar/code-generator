package io.github.muehmar.codegenerator.java;

public enum JavaModifier {
  PRIVATE("private", 0),
  PROTECTED("protected", 0),
  PUBLIC("public", 0),
  DEFAULT("default", 0),
  STATIC("static", 1),
  ABSTRACT("abstract", 1),
  FINAL("final", 2),
  SEALED("sealed", 3),
  NON_SEALED("non-sealed", 3),
  ;

  private final String value;
  private final int order;

  JavaModifier(String value, int order) {
    this.value = value;
    this.order = order;
  }

  public int getOrder() {
    return order;
  }

  public String asString() {
    return value;
  }
}
