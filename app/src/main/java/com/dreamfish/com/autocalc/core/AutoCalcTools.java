package com.dreamfish.com.autocalc.core;

import java.math.BigDecimal;
import java.util.Random;
import java.util.regex.Pattern;

import static com.dreamfish.com.autocalc.core.AutoCalc.*;

public class AutoCalcTools {


  public AutoCalcTools(AutoCalc autoCalc) {
    this.autoCalc = autoCalc;
  }

  private AutoCalc autoCalc = null;


  /**
   * 角度转弧度
   *
   * @param d 角度
   */
  public BigDecimal d2g(BigDecimal d) {
    return d.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180d), autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
  }
  /**
   * 弧度转角度
   *
   * @param g 弧度
   */
  public BigDecimal g2d(BigDecimal g) {
    return g.multiply(BigDecimal.valueOf(180d)).divide(BigDecimal.valueOf(Math.PI), autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP);
  }
  /**
   * 计算对数(BigDecimal精确)
   */
  public BigDecimal log(BigDecimal b, BigDecimal a) {
    return BigDecimal.valueOf(Math.log(b.doubleValue()) / Math.log(a.doubleValue()));
  }
  /**
   * 生成 min 到 max 之间的随机数
   */
  public Long rand(Long min, Long max) {
    return min + ((new Random().nextLong() * (max - min)));
  }
  /**
   * 计算阶乘
   * @param d d
   */
  public long fact(long d) {
    //
    long sum = 1;
    for (int i = 1; i <= d; i++)
      sum = sum * i;
    return sum;
  }

  /**
   * 数字转字符串
   *
   * @param n BigDecimal数字
   * @return 返回转为的字符串
   */
  public String numberToStr(double n) throws AutoCalcException, AutoCalcInfiniteException {
    return numberToStr(doubleToBigDecimal(n));
  }
  /**
   * 数字转字符串
   *
   * @param n BigDecimal数字
   * @return 返回转为的字符串
   */
  public String numberToStr(BigDecimal n) {
    switch (autoCalc.getBcMode()) {
      case BC_MODE_DEC:
        return n.setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP).toPlainString();
      case BC_MODE_BIN:
        return Long.toBinaryString(n.setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP).longValue());
      case BC_MODE_OCT:
        return Long.toOctalString(n.setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP).longValue());
      case BC_MODE_HEX:
        return Long.toHexString(n.setScale(autoCalc.getNumberScale(), BigDecimal.ROUND_HALF_UP).longValue());
    }
    return "";
  }
  /**
   * 数字转字符串
   *
   * @param n BigDecimal数字
   * @return 返回转为的字符串
   */
  public String numberToStr(long n) {
    switch (autoCalc.getBcMode()) {
      case BC_MODE_DEC:
        return String.valueOf(n);
      case BC_MODE_BIN:
        return Long.toBinaryString(n);
      case BC_MODE_OCT:
        return Long.toOctalString(n);
      case BC_MODE_HEX:
        return Long.toHexString(n);
    }
    return "";
  }
  /**
   * 数字字符串转为数字
   *
   * @param str 字符串
   * @return 返回转为的数字
   */
  public BigDecimal strToNumber(String str) {
    double result;
    if (str.startsWith(" ") || str.endsWith(" ")) str = str.trim();

    if (str.endsWith("b")) result = Long.valueOf(str.substring(0, str.length() - 1), 2);
    else if (str.startsWith("0b")) result = Long.valueOf(str.substring(2), 2);
    else if (autoCalc.getBcMode() == BC_MODE_BIN) result = Long.valueOf(str, 2);
    else if (str.endsWith("o")) result = Long.valueOf(str.substring(0, str.length() - 1), 8);
    else if (str.startsWith("0o")) result = Long.valueOf(str.substring(2), 8);
    else if (autoCalc.getBcMode() == BC_MODE_OCT) result = Long.valueOf(str, 8);
    else if (str.endsWith("h")) result = Long.valueOf(str.substring(0, str.length() - 1), 16);
    else if (str.startsWith("0x")) result = Long.valueOf(str.substring(2), 16);
    else if (autoCalc.getBcMode() == BC_MODE_HEX) result = Long.valueOf(str, 16);
    else result = Double.valueOf(str);
    return BigDecimal.valueOf(result);
  }
  /**
   * 检查字符串是否是数字
   *
   * @param str 需要检查的字符
   */
  public boolean isNumber(String str) {
    if ("".equals(str)) return false;
    if (str.startsWith(" ") || str.endsWith(" ")) str = str.trim();
    if (str.endsWith("b") || str.startsWith("0b") || autoCalc.getBcMode() == BC_MODE_BIN) {
      if(str.endsWith("b")) str = str.substring(0, str.length() - 1);
      if(str.startsWith("0b")) str = str.substring(2);

      return Pattern.matches("-?[0-1]*(\\.?)[0-1]*", str);
    }
    else if (str.endsWith("o") || str.startsWith("0o") || autoCalc.getBcMode() == BC_MODE_OCT) {
      if(str.endsWith("o")) str = str.substring(0, str.length() - 1);
      if(str.startsWith("0o")) str = str.substring(2);

      return Pattern.matches("-?[0-7]*(\\.?)[0-7]*", str);
    }
    else if (str.endsWith("h") || str.startsWith("0x") || autoCalc.getBcMode() == BC_MODE_HEX) {

      if(str.endsWith("h")) str = str.substring(0, str.length() - 1);
      if(str.startsWith("0x")) str = str.substring(2);

      return Pattern.matches("-?([0-9]|[a-f]|[A-F])*(\\.?)([0-9]|[a-f]|[A-F])*", str);
    }
    else if (str.endsWith("d") || autoCalc.getBcMode() == BC_MODE_DEC) {

      if(str.endsWith("d")) str = str.substring(0, str.length() - 1);
      return Pattern.matches("-?[0-9]*(\\.?)[0-9]*", str);
    }
    return Pattern.matches("-?[0-9]*(\\.?)[0-9]*", str);
  }
  /**
   * 字符串转数字
   * @param stringBuilder 字符串
   * @return 数字BigDecimal
   */
  public BigDecimal strToNumber(StringBuilder stringBuilder) {
    return strToNumber(stringBuilder.toString());
  }
  /**
   * 检查是否是数字
   * @param stringBuilder 字符串
   * @return 是否是数字
   */
  public boolean isNumber(StringBuilder stringBuilder) {
    return isNumber(stringBuilder.toString());
  }

  public BigDecimal doubleToBigDecimal(Double d) throws AutoCalcException, AutoCalcInfiniteException {
    if(d.isNaN()) throw new AutoCalcException("计算出错");
    if(d.isInfinite()) throw new AutoCalcInfiniteException();
    return BigDecimal.valueOf(d);
  }

  /**
   * 获取数字的进制（h,o,b）
   * @param str 数字字符串
   * @return 进制（h,o,b）
   */
  public String getNumberStrRadix(String str) {
    if (str.startsWith(" ") || str.endsWith(" ")) str = str.trim();
    if (str.endsWith("b") || str.startsWith("0b")) return "b";
    else if (str.endsWith("o") || str.startsWith("0o")) return "o";
    else if (str.endsWith("h") || str.startsWith("0x")) return "h";
    return "d";
  }


}

