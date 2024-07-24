package io.github.muehmar.codegenerator.writer;

import lombok.Value;

@Value
public class WriterSettings {
  int spacesPerTab;
  boolean noMultipleNewLines;

  public static WriterSettings defaultSettings() {
    return new WriterSettings(2, true);
  }
}
