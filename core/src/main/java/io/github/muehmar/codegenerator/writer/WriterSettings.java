package io.github.muehmar.codegenerator.writer;

import lombok.Value;

@Value
public class WriterSettings {
  int spacesPerTab;

  public static WriterSettings defaultSettings() {
    return new WriterSettings(2);
  }
}
