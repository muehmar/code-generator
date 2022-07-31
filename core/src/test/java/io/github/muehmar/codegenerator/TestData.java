package io.github.muehmar.codegenerator;

import ch.bluecare.commons.data.PList;
import lombok.Value;

public class TestData {
  private TestData() {}

  public static Void noData() {
    return null;
  }

  public static BooleanData booleanData(boolean flag) {
    return new BooleanData(flag);
  }

  public static ListData<StringData> stringListData(String... data) {
    return new ListData<>(PList.fromArray(data).map(StringData::new));
  }

  @Value
  public static class BooleanData {
    boolean flag;
  }

  @Value
  public static class StringData {
    String text;
  }

  @Value
  public static class ListData<T> {
    PList<T> list;
  }
}
