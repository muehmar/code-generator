package io.github.muehmar.codegenerator;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Generator<A, B> {
  Writer generate(A data, B settings, Writer writer);

  static <A, B> Generator<A, B> of(Generator<A, B> gen) {
    return gen;
  }

  static <A, B> Generator<A, B> constant(String constant) {
    return (data, settings, writer) -> writer.println(constant);
  }

  static <A, B> Generator<A, B> ofWriterFunction(UnaryOperator<Writer> f) {
    return (data, settings, writer) -> f.apply(writer);
  }

  static <A, B> Generator<A, B> emptyGen() {
    return (data, settings, writer) -> writer;
  }

  static <A, B> Generator<A, B> newLine() {
    return (data, settings, writer) -> writer.println();
  }

  default Generator<A, B> appendNewLine() {
    return append((UnaryOperator<Writer>) Writer::println);
  }

  default Generator<A, B> prependNewLine() {
    return Generator.<A, B>emptyGen().appendNewLine().append(this);
  }

  default Generator<A, B> append(Generator<A, B> next) {
    final Generator<A, B> self = this;
    return (data, settings, writer) ->
        next.generate(data, settings, self.generate(data, settings, writer));
  }

  default Generator<A, B> appendNoSettings(Generator<A, Void> next) {
    final Generator<A, B> self = this;
    return (data, settings, writer) ->
        next.generate(data, (Void) null, self.generate(data, settings, writer));
  }

  default Generator<A, B> append(Generator<A, B> next, int tabs) {
    final Generator<A, B> self = this;
    return (data, settings, writer) ->
        self.generate(data, settings, writer)
            .append(tabs, next.generate(data, settings, writer.empty()));
  }

  default Generator<A, B> append(UnaryOperator<Writer> next) {
    return append((data, settings, writer) -> next.apply(writer));
  }

  default <C> Generator<A, B> append(Generator<C, B> gen, Function<A, C> f) {
    final Generator<A, B> self = this;
    return (data, settings, writer) ->
        gen.generate(f.apply(data), settings, self.generate(data, settings, writer));
  }

  default <C> Generator<A, B> appendList(Generator<C, B> gen, Function<A, Iterable<C>> f) {
    return appendList(gen, f, Generator.emptyGen());
  }

  default <C> Generator<A, B> appendList(
      Generator<C, B> gen, Function<A, Iterable<C>> f, Generator<A, B> separator) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return PList.fromIter(f.apply(data))
          .<UnaryOperator<Writer>>map(e -> w -> gen.generate(e, settings, w))
          .reduce((f1, f2) -> w -> f2.apply(separator.generate(data, settings, f1.apply(w))))
          .map(f1 -> f1.apply(selfWriter))
          .orElse(selfWriter);
    };
  }

  default Generator<A, B> appendConditionally(BiPredicate<A, B> predicate, Generator<A, B> append) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      if (predicate.negate().test(data, settings)) {
        return self.generate(data, settings, writer);
      }
      return append(append).generate(data, settings, writer);
    };
  }

  default Generator<A, B> appendConditionally(Predicate<A> predicate, Generator<A, B> append) {
    return appendConditionally((data, settings) -> predicate.test(data), append);
  }

  /**
   * Returns a new {@link Generator} whose input data is transformed before the current generator is
   * applied.
   */
  default <C> Generator<C, B> contraMap(Function<C, A> f) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> self.generate(f.apply(data), settings, writer);
  }

  /**
   * Filters the current generator, i.e. if the given predicate does not hold true, the returned
   * generator is an empty generator.
   */
  default Generator<A, B> filter(BiPredicate<A, B> predicate) {
    final Generator<A, B> self = this;
    return ((data, settings, writer) -> {
      if (predicate.test(data, settings)) {
        return self.generate(data, settings, writer);
      }
      return writer;
    });
  }

  /**
   * Filters the current generator, i.e. if the given predicate does not hold true, the returned
   * generator is an empty generator.
   */
  default Generator<A, B> filter(Predicate<A> predicate) {
    return filter((data, settings) -> predicate.test(data));
  }
}
