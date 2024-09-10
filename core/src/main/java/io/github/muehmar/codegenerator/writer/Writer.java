package io.github.muehmar.codegenerator.writer;

import static io.github.muehmar.codegenerator.writer.WriterSettings.defaultSettings;

import ch.bluecare.commons.data.PList;
import ch.bluecare.commons.data.Pair;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Writer {
  private static final String NEWLINE_STRING = "\n";

  private final PList<String> refs;
  private final int refsLineNumber;
  private final PList<Line> lines;

  private final String tab;

  private final int tabs;
  private final boolean newline;

  private WriterSettings settings;

  private final RefWriter refWriter;

  public static Writer create(RefWriter refWriter, WriterSettings settings) {
    final String tab = new String(new char[settings.getSpacesPerTab()]).replace("\0", " ");
    return new Writer(
        PList.empty(), -1, PList.single(Line.empty()), tab, 0, true, settings, refWriter);
  }

  public static Writer javaWriter() {
    return javaWriter(defaultSettings());
  }

  public static Writer javaWriter(WriterSettings settings) {
    return create(new JavaRefWriter(), settings);
  }

  private Writer appendToLastLine(String fragment) {
    final PList<Line> newLines =
        this.lines
            .headOption()
            .map(l -> l.append(fragment))
            .map(l -> newline ? l.prepend(createTabs(tabs)) : l)
            .map(l -> this.lines.tail().cons(l))
            .orElse(this.lines);
    return new Writer(refs, refsLineNumber, newLines, tab, tabs, false, settings, refWriter);
  }

  public Writer tab(int tabs) {
    return new Writer(refs, refsLineNumber, lines, tab, tabs, newline, settings, refWriter);
  }

  public Writer append(Writer other) {
    return append(0, other);
  }

  public Writer append(int tabs, Writer other) {
    final int usedRefsLineNumber =
        this.refsLineNumber > 0 ? this.refsLineNumber : other.refsLineNumber;

    final UnaryOperator<Line> indentLine = line -> line.prepend(createTabs(tabs));

    final PList<Line> newLines =
        other.getLinesDroppingLastNewline().map(indentLine).concat(getLinesDroppingLastNewline());

    return new Writer(
        this.refs.concat(other.refs),
        usedRefsLineNumber,
        newLines.cons(Line.empty()),
        tab,
        0,
        true,
        settings,
        refWriter);
  }

  private PList<String> createTabs(int tabs) {
    return PList.range(0, tabs).map(ignore -> tab);
  }

  private PList<Line> getLinesDroppingLastNewline() {
    return newline ? lines.drop(1) : lines;
  }

  public Writer empty() {
    return new Writer(
        PList.empty(), -1, PList.single(Line.empty()), tab, 0, true, settings, refWriter);
  }

  public Writer ref(String ref) {
    return new Writer(
        refs.cons(ref), refsLineNumber, lines, tab, tabs, newline, settings, refWriter);
  }

  public Writer refs(Iterable<String> ref) {
    return new Writer(
        refs.concat(PList.fromIter(ref)),
        refsLineNumber,
        lines,
        tab,
        tabs,
        newline,
        settings,
        refWriter);
  }

  public PList<String> getRefs() {
    return refs;
  }

  /**
   * This will print the refs at the current position utilizing {@link RefWriter} when calling
   * {@link Writer#asString()}.
   */
  public Writer printRefs() {
    return new Writer(
        refs, lines.size() - (newline ? 1 : 0), lines, tab, tabs, newline, settings, refWriter);
  }

  private static String removeTrailingNewlineCharacter(String str) {
    final int length = str.length();
    if (length > 0 && str.substring(length - 1).equals(NEWLINE_STRING)) {
      return str.substring(0, length - 1);
    }
    return str;
  }

  /**
   * Creates a single blank line, if there is not already a blank line. Does nothing in case there
   * is already a blank line.
   */
  public Writer printSingleBlankLine() {
    if (lines.headOption().filter(Line::isEmpty).isPresent()
        && lines.drop(1).headOption().filter(Line::isEmpty).isPresent()) {
      return this;
    }
    return println();
  }

  public Writer print(String string, Object... args) {
    return appendToLastLine(String.format(string, args));
  }

  public Writer print(char value) {
    return print("" + value);
  }

  public Writer print(int value) {
    return print("" + value);
  }

  public Writer println(char value) {
    return print(value).println();
  }

  public Writer println(int value) {
    return print(value).println();
  }

  public Writer println(String string) {
    return print(string).println();
  }

  public Writer println(String string, Object... args) {
    return print(string, args).println();
  }

  public Writer println() {
    return new Writer(
        refs, refsLineNumber, lines.cons(Line.empty()), tab, 0, true, settings, refWriter);
  }

  /**
   * Removes any trailing empty lines, i.e. this method is called and print statement will add the
   * content directly to the last non empty line.
   */
  public Writer resetToLastNotEmptyLine() {
    final AtomicBoolean nonEmptyFound = new AtomicBoolean(false);
    final PList<Line> filteredLines =
        lines.filter(
            line -> {
              if (line.nonEmpty() || nonEmptyFound.get()) {
                nonEmptyFound.set(true);
                return true;
              } else {
                return false;
              }
            });
    return new Writer(refs, refsLineNumber, filteredLines, tab, tabs, false, settings, refWriter);
  }

  /** Returns the content of this writer as string- */
  public String asString() {
    final StringBuilder sb = new StringBuilder();
    final Consumer<Line> applyStringBuilder =
        line -> {
          sb.append(line.removeTrailingBlankFragments().asStringBuilder());
          sb.append(NEWLINE_STRING);
        };

    final BiPredicate<Pair<Line, Integer>, Pair<Line, Integer>> consecutiveLineFilter =
        settings.isNoMultipleNewLines()
            ? (l1, l2) ->
                l1.first().nonEmpty()
                    || l2.first().nonEmpty()
                    || (l2.second() == refsLineNumber && refs.nonEmpty())
            : (l1, l2) -> true;

    final PList<Line> reversedLines =
        getLinesDroppingLastNewline()
            .reverse()
            .zipWithIndex()
            .filterWithPrev(ignore -> true, consecutiveLineFilter)
            .map(Pair::first);
    reversedLines.take(Math.max(refsLineNumber, 0)).forEach(applyStringBuilder);

    if (refsLineNumber >= 0) {
      refs.distinct(Function.identity())
          .sort(refWriter.sortComparator())
          .filter(refWriter.filter())
          .map(refWriter::format)
          .forEach(ref -> sb.append(ref).append(NEWLINE_STRING));
    }

    reversedLines.drop(Math.max(refsLineNumber, 0)).forEach(applyStringBuilder);
    return removeTrailingNewlineCharacter(sb.toString());
  }
}
