package io.github.muehmar.codegenerator;

import ch.bluecare.commons.data.PList;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Generator<A, B> {
  /**
   * Appends content to the given {@link Writer} for the data {@link A} and the settings {@link B}
   * by returning a new immutable instance of {@link Writer} containing the new content.
   */
  Writer generate(A data, B settings, Writer writer);

  static <A, B> Generator<A, B> of(Generator<A, B> gen) {
    return gen;
  }

  /** Creates a new {@link Generator} producing the given constant. */
  static <A, B> Generator<A, B> constant(String constant) {
    return (data, settings, writer) -> writer.println(constant);
  }

  /** Creates a new {@link Generator} by applying the given function on the {@link Writer}. */
  static <A, B> Generator<A, B> ofWriterFunction(UnaryOperator<Writer> f) {
    return (data, settings, writer) -> f.apply(writer);
  }

  /** Creates a new {@link Generator} which produces nothing. */
  static <A, B> Generator<A, B> emptyGen() {
    return (data, settings, writer) -> writer;
  }

  /** Creates a new {@link Generator} which produces a new line. */
  static <A, B> Generator<A, B> newLine() {
    return (data, settings, writer) -> writer.println();
  }

  /**
   * Appends a single blank line if there is not already a blank line. If there is already a blank
   * line, nothing wil get appended.
   */
  default Generator<A, B> appendSingleBlankLine() {
    return append(Writer::printSingleBlankLine);
  }

  /** Returns a new {@link Generator} which appends a new line to {@code this}. */
  default Generator<A, B> appendNewLine() {
    return append((UnaryOperator<Writer>) Writer::println);
  }

  /** Returns a new {@link Generator} which prepends a new line to {@code this}. */
  default Generator<A, B> prependNewLine() {
    return Generator.<A, B>emptyGen().appendNewLine().append(this);
  }

  /** Returns a new {@link Generator} which is {@code this} indented by the given amout of tabs. */
  default Generator<A, B> indent(int tabs) {
    return Generator.<A, B>emptyGen().append(this, tabs);
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this}.
   */
  default Generator<A, B> append(Generator<A, B> next) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return next.generate(data, settings, selfWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this}, where the next generator has no settings.
   */
  default Generator<A, B> appendNoSettings(Generator<A, Void> next) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return next.generate(data, (Void) null, selfWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this} intended with the given number of tabs.
   */
  default Generator<A, B> append(Generator<A, B> next, int tabs) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      final Writer nextWriter = next.generate(data, settings, writer.empty());
      return selfWriter.append(tabs, nextWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content produced by the given {@link
   * Writer} function {@code next} to the content of {@code this}.
   */
  default Generator<A, B> append(UnaryOperator<Writer> next) {
    return append((data, settings, writer) -> next.apply(writer));
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this} while transforming the input data with the given
   * function {@code f} for the next generator.
   */
  default <C> Generator<A, B> append(Generator<C, B> gen, Function<A, C> f) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return gen.generate(f.apply(data), settings, selfWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this}. The next generator is executed for every element
   * which is returned by the given function {@code f} applied to the input data. The new content is
   * appended in the order of the elements returned by the function {@code f}.
   */
  default <C> Generator<A, B> appendList(Generator<C, B> gen, Function<A, Iterable<C>> f) {
    return appendList(gen, f, Generator.emptyGen());
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this}. The next generator is executed for every element
   * which is returned by the given function {@code f} applied to the input data.
   *
   * <p>Between the content of each element produced by the {@code next} generator, content produced
   * by the {@code separator} generator.
   *
   * <p>The new content is appended in the order of the elements returned by the function {@code f}.
   */
  default <C> Generator<A, B> appendList(
      Generator<C, B> next, Function<A, Iterable<C>> f, Generator<A, B> separator) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return PList.fromIter(f.apply(data))
          .<UnaryOperator<Writer>>map(e -> w -> next.generate(e, settings, w))
          .reduce((f1, f2) -> w -> f2.apply(separator.generate(data, settings, f1.apply(w))))
          .map(f1 -> f1.apply(selfWriter))
          .orElse(selfWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this} if the mapping function {@code f} returns a
   * non-empty {@link Optional}.
   */
  default <C> Generator<A, B> appendOptional(Generator<C, B> next, Function<A, Optional<C>> f) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      final Writer selfWriter = self.generate(data, settings, writer);
      return f.apply(data).map(c -> next.generate(c, settings, selfWriter)).orElse(selfWriter);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this} only if the given {@link Predicate} holds true;
   */
  default Generator<A, B> appendConditionally(BiPredicate<A, B> predicate, Generator<A, B> append) {
    final Generator<A, B> self = this;
    return (data, settings, writer) -> {
      if (predicate.negate().test(data, settings)) {
        return self.generate(data, settings, writer);
      }
      return append(append).generate(data, settings, writer);
    };
  }

  /**
   * Returns a new {@link Generator} which will append the content of the given {@link Generator}
   * {@code next} to the content of {@code this} only if the given {@link Predicate} holds true;
   */
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
