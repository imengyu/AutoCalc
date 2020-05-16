package com.dreamfish.com.autocalc.item.converter;

import android.widget.Button;
import android.widget.TextView;

import com.dreamfish.com.autocalc.core.AutoCalc;
import com.dreamfish.com.autocalc.core.AutoCalcException;
import com.dreamfish.com.autocalc.core.AutoCalcInfiniteException;
import com.dreamfish.com.autocalc.utils.TextUtils;

import java.math.BigDecimal;
import java.util.function.BiPredicate;

public class ConverterItem {

  private BigDecimal base = BigDecimal.ZERO;

  private BigDecimal benchmark = BigDecimal.ZERO;
  private BigDecimal input = BigDecimal.ZERO;

  private boolean valueOverflow = false;

  public boolean isValueOverflow() {
    return valueOverflow;
  }

  private TextView resultView;
  private TextView unitView;
  private Button unitChooser;

  private AutoCalc autoCalc;

  private ConverterData currentData;

  public ConverterItem(AutoCalc autoCalc, TextView resultView, TextView unitView, Button unitChooser) {
    this.autoCalc = autoCalc;
    this.resultView = resultView;
    this.unitView = unitView;
    this.unitChooser = unitChooser;
  }

  public BigDecimal calculate() throws Exception {
    if(TextUtils.isNumber(textBuffer)
            && !textBuffer.toString().endsWith(".")
            && !textBuffer.toString().equals("-")) {
      input = new BigDecimal(textBuffer.toString());

      if(input.compareTo(currentData.minVal) < 0) {
        input = currentData.minVal;
        valueOverflow = true;
      }
      else if(input.compareTo(currentData.maxVal) > 0) {
        input = currentData.maxVal;
        valueOverflow = true;
      }
      else valueOverflow = false;

      if(currentData.calcType == ConverterData.CALC_TYPE_MULTIPLY) {
        if(input.compareTo(BigDecimal.ZERO) == 0 || base.compareTo(BigDecimal.ZERO) == 0)
          benchmark = BigDecimal.ZERO;
        else benchmark = input.multiply(base)
                .setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
      }else if(currentData.calcType == ConverterData.CALC_TYPE_DIFFER) {
        benchmark = input.subtract(BigDecimal.ONE)
                .setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
      }
      else if(currentData.calcType == ConverterData.CALC_TYPE_FORMULA) {

        benchmark = autoCalc.calcBigDecimal(
                currentData.calcFormulaToBenchmark
                        .replaceAll("%val%", input.toString())
                        .replaceAll("%base%", base.toString()));
      }

    }

    return benchmark;
  }
  public void fromBenchmark(BigDecimal b) throws Exception {

    if(currentData.calcType == ConverterData.CALC_TYPE_MULTIPLY) {
      if(b.compareTo(BigDecimal.ZERO) == 0 || base.compareTo(BigDecimal.ZERO) == 0)
        input = BigDecimal.ZERO;
      else input = b.divide(base, autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP)
              .setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
    }
    else if(currentData.calcType == ConverterData.CALC_TYPE_DIFFER) {
      input = b.add(base).setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
    }
    else if(currentData.calcType == ConverterData.CALC_TYPE_FORMULA) {
      if(currentData.stringConversion) {
        textBuffer.delete(0, textBuffer.length());
        textBuffer.append(autoCalc.calc(currentData.calcFormulaFromBenchmark
                .replaceAll("%val%", b.toString())
                .replaceAll("%base%", base.toString())));
      }else input = autoCalc.calcBigDecimal(currentData.calcFormulaFromBenchmark
              .replaceAll("%val%", b.toString())
              .replaceAll("%base%", base.toString()));

    }

    if(input.compareTo(currentData.minVal) < 0) input = currentData.minVal;
    else if(input.compareTo(currentData.maxVal) > 0) input = currentData.maxVal;
  }
  public void forceUpdateToView(String format) {
    this.resultView.setText(format);
  }
  public void updateToView(Boolean format) {
    updateToView(format, "");
  }
  public void updateToView(Boolean format, String appendText) {
    String rs;
    StringBuilder text = new StringBuilder(appendText);
    text.append(' ');

    if(!currentData.stringConversion && format) {
      if (input.compareTo(BigDecimal.valueOf(autoCalc.getScientificNotationMax())) >= 0) {
        try {
          rs = autoCalc.getTools().numberToScientificNotationStr(input);
        } catch (AutoCalcException e) {
          rs = e.getMessage();
        } catch (AutoCalcInfiniteException e) {
          rs = "∞";
        }
      } else rs = this.input.stripTrailingZeros().toPlainString();

      textBuffer.delete(0, textBuffer.length());
      textBuffer.append(rs);

      text.append(textBuffer);

      this.resultView.setText(text);
    }
    else {
      text.append(textBuffer);
      this.resultView.setText(text);
    }
  }
  public void updateUnitData(ConverterData data) {

    currentData = data;
    base = BigDecimal.valueOf(currentData.unitBase);
    unitView.setText(currentData.unitNameShort);
    unitChooser.setText(currentData.unitName);

  }

  public boolean getCanBeNegative() {
    return currentData.min < 0;
  }
  public boolean getCanSelect() {
    return currentData != null && currentData.selectable;
  }
;
  private StringBuilder textBuffer = new StringBuilder("0");

  //Text input control
  public void clearText() {
    valueOverflow = false;
    textBuffer = new StringBuilder("0");
    writeText("0");
  }
  public void writeText(String s) {

    if(s.equals(".") && TextUtils.containsChar(textBuffer, '.'))
      return;

    if (textBuffer.length() == 1 && textBuffer.charAt(0) == '0' && !s.equals(".")) textBuffer = new StringBuilder(s);
    else if (s.equals("-")) {
      if(TextUtils.containsChar(textBuffer, '-')) textBuffer.delete(0, 1);
      else textBuffer.insert(0, '-');
    }
    else if (textBuffer.length() < 100) textBuffer.append(s);
  }
  public void delText() {
    if(textBuffer.length() > 0)
      textBuffer.deleteCharAt(textBuffer.length() - 1);
    if(textBuffer.length() == 0)
      textBuffer.append("0");
  }


}
