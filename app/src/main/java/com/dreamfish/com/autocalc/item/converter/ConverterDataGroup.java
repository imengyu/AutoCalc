package com.dreamfish.com.autocalc.item.converter;

import java.util.ArrayList;
import java.util.List;

public class ConverterDataGroup {

  public static final int MAX_ONEPAGE_CONVERTER_COUNT = 5;

  public ConverterDataGroup(String name) {
    group = new ArrayList<>();
    defalutIndex = new int [MAX_ONEPAGE_CONVERTER_COUNT];
    this.name = name;
  }

  private List<ConverterData> group;
  private int baseIndex = 0;
  private int[] defalutIndex;
  private String name;

  public String getName() {
    return name;
  }
  public List<ConverterData> getGroup() {
    return group;
  }
  public int getBaseIndex() {
    return baseIndex;
  }
  public void setBaseIndex(int baseIndex) {
    this.baseIndex = baseIndex;
  }
  public int[] getDefalutIndex() {
    return defalutIndex;
  }
  public void add(ConverterData data) { group.add(data); }
}
