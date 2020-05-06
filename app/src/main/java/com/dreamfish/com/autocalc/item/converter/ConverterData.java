package com.dreamfish.com.autocalc.item.converter;

import java.math.BigDecimal;

public class ConverterData {

  public static final int CALC_TYPE_MULTIPLY = 0;
  public static final int CALC_TYPE_DIFFER = 1;
  public static final int CALC_TYPE_FORMULA = 2;

  public String unitName;
  public String unitNameShort = "";

  public Double min = 0.0;
  public Double max = Double.MAX_VALUE;
  public BigDecimal minVal = BigDecimal.ZERO;
  public BigDecimal maxVal = BigDecimal.valueOf(max);

  public Double unitBase = 1.0;
  public int calcType = CALC_TYPE_MULTIPLY;
  public String calcFormulaToBenchmark = "";
  public String calcFormulaFromBenchmark = "";


  public boolean isTitle = false;
  public boolean selectable = true;
  public boolean stringConversion = false;

  public ConverterData(String title) {
    unitName = title;
    isTitle = true;
  }
  public ConverterData(String name, String shortName) {
    unitName = name;
    unitNameShort = shortName;
  }

  public void setMinVal(double v) {
    min = v;
    minVal = BigDecimal.valueOf(v);
  }
  public void setMaxVal(double v) {
    max = v;
    maxVal = BigDecimal.valueOf(v);
  }
}
