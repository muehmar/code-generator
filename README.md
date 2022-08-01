[![Build Status](https://github.com/muehmar/code-generator/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/muehmar/code-generator/actions/workflows/gradle.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/muehmar/code-generator/blob/master/LICENSE)

# Code Generator

This library provides a generator for arbitrary source code. The generator is composable, i.e. complex generators can be
created out of simpler generators.

This library was primarily created to generate Java source code for
the [Pojo-Extension](https://github.com/muehmar/pojo-extension) and
the [OpenAPI Generator](https://github.com/muehmar/gradle-openapi-schema) projects. But it might be useful for other
projects too.

The `Generator` interface defines a single method:

```
Writer generate(A data, B settings, Writer writer);
```

It accepts a data object and a settings object as well as the `Writer` instance. The `Writer` class is immutable, the
method returns a new `Writer` instance with the additional generated content.

The `Generator` interface contains a bunch of default methods for composing different generators to a single new
generator.

## Java Generators

The library contains already some predefined generators for creating Java source code. These classes are created for the
mentioned projects and may be not yet complete.

The generators can be created with a fluent builder (with the help
of [Pojo-Extension](https://github.com/muehmar/pojo-extension)), one can use the static factory methods
in `JavaGenerators`:

```
Generator<String, Void> classGenerator = JavaGenerators.<String, Void>classGen()
        .clazz()
        .topLevel()
        .packageGen(Generator.constant("package io.github.muehmar;"))
        .modifiers(PUBLIC)
        .className("HelloWorld")
        .noSuperClass()
        .noInterfaces()
        .content((data, settings, writer) -> writer.println(data))
        .build()
```