package com.dreamfish.com.autocalc.core;

/**
 * AutoCalc 在计算中发生无限大异常
 */
public class AutoCalcInfiniteException extends Exception {
  public AutoCalcInfiniteException() {
    super("Calc Infinite");
  }
}