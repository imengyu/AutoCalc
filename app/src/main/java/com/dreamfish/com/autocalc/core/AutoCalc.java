package com.dreamfish.com.autocalc.core;

import android.os.Build;

import com.dreamfish.com.autocalc.utils.ConvertUpMoney;

import java.util.*;
import java.math.*;

//
//此类仅为计算用户输入的算式
//程序需要计算-请直接用代码
//效率一般
//这是原始人写的代码
//说真的，是我写的，但现在我已经看不懂了。。。
//so，祈祷它没啥问题吧，然后不用去理它了
//毕竟，写一个新的或许会更好更快
//在 2018/9 写的
//   2020/1 修复了一次
//   2020/3 重构了一次
//
//1+3-6*(5-3)
//

/**
 * 自动算式计算
 *
 * @author DreamFish
 */
public class AutoCalc {

  public static final int BC_MODE_DEC = 0;
  public static final int BC_MODE_BIN = 1;
  public static final int BC_MODE_HEX = 2;
  public static final int BC_MODE_OCT = 3;

  /**
   * Tools
   */
  private AutoCalcTools tools;
  /**
   * Math
   */
  private AutoCalcMath math;

  /**
   * 创建实例
   */
  public AutoCalc() {
    tools = new AutoCalcTools(this);
    math = new AutoCalcMath(this);

    initAllFunctionSolver();
    initAllOperatorSolver();
    initAllConstants();
  }


  /**
   * 计算入口
   * 直接传入需要计算的算式
   * 返回就是结果
   *
   * @param equation 需要计算的算式
   */
  public String calc(String equation) {
    if ("".equals(equation))// 空检测
      return "请输入算式";
    if (equation.startsWith("="))// 处理掉=
      equation = equation.substring(1);// 取=后面的
    if (equation.endsWith("="))// 处理掉=
      equation = equation.substring(0, equation.length() - 1);// 取=后面的
    try {
      // 计算
      forceNoNumberCheck = false;
      lastSuccess = true;
      lastCalcSteps.clear();
      lastContainsBinaryConversion = false;
      lastFormula = equation;

      StringBuilder formulaBuffer = new StringBuilder(equation);
      calcCore(formulaBuffer, true);

      equation = formulaBuffer.toString();

      if(!forceNoNumberCheck && tools.isNumber(equation) && autoScientificNotation) {
        BigDecimal n = tools.strToNumber(equation);
        if(n.compareTo(BigDecimal.valueOf(scientificNotationMax)) >= 0)
          return tools.numberToScientificNotationStr(n);
      }

      return equation;
    } catch (AutoCalcException e) {
      lastSuccess = false;
      lastException = e;
      return e.getMessage();
    } catch (AutoCalcInfiniteException e) {
      lastSuccess = true;
      return "∞";
    } catch (Exception e) {
      // 异常信息抓取
      lastSuccess = false;
      lastException = e;
      e.printStackTrace();
      return e.getMessage();
    }
  }

  /**
   * 计算入口
   * @param equation 算式
   * @return 结果
   */
  public BigDecimal calcBigDecimal(String equation) throws Exception {
    forceNoNumberCheck = false;
    lastSuccess = true;
    lastCalcSteps.clear();
    lastContainsBinaryConversion = false;
    lastFormula = equation;

    StringBuilder formulaBuffer = new StringBuilder(equation);
    calcCore(formulaBuffer, true);

    equation = formulaBuffer.toString();

    if(tools.isNumber(equation))
      return tools.strToNumber(equation);

    throw new AutoCalcException("不是数字");
  }


  public AutoCalcMath getMath() {
    return math;
  }
  public AutoCalcTools getTools() {
    return tools;
  }

  /**
   * 检查算式中是否有运算符算式
   *
   * @param equation 算式
   * @return 是否有运算符
   */
  public boolean testHasOperator(String equation) {
    boolean hasOp = false;
    for (int i = 0; i < equation.length(); i++) {
      hasOp = isOperator(equation.charAt(i));
      if (hasOp) break;
    }
    return hasOp;
  }

  /**
   * 检查算式是否是数字
   *
   * @param equation 算式
   * @return 否是数字
   */
  public boolean testIsNumber(String equation) {
    return tools.isNumber(equation);
  }

  public void resetLastSuccess() {
    lastSuccess = true;
  }

  /**
   * 获取上次计算是否成功
   *
   * @return 上次计算是否成功
   */
  public boolean isLastSuccess() {
    return lastSuccess;
  }

  /**
   * 获取是否使用角度制
   *
   * @return 是否使用角度制
   */
  public boolean isUseDegree() {
    return useDegree;
  }

  /**
   * 设置是否使用角度制
   *
   * @param useDegree 是否使用角度制
   */
  public void setUseDegree(boolean useDegree) {
    this.useDegree = useDegree;
  }

  /**
   * 获取是否将求余%变成百分号
   *
   * @return 是否将求余%变成百分号
   */
  public boolean isUseModAsPercent() {
    return useModAsPercent;
  }

  /**
   * 设置是否将求余%变成百分号
   *
   * @param useModAsPercent 是否将求余%变成百分号
   */
  public void setUseModAsPercent(boolean useModAsPercent) {
    this.useModAsPercent = useModAsPercent;
  }

  /**
   * 获取计算器进制
   *
   * @return 进制
   */
  public int getBcMode() {
    return bcMode;
  }

  /**
   * 设置计算器进制
   *
   * @param bcMode 进制
   */
  public void setBcMode(int bcMode) {
    this.bcMode = bcMode;
  }

  /**
   * 获取计算精度
   */
  public int getNumberScale() {
    return numberScale;
  }

  /**
   * 设置计算精度
   */
  public void setNumberScale(int numberScale) {
    this.numberScale = numberScale;
  }

  /**
   * 获取上次计算发生的错误
   */
  public Exception getLastException() {
    return lastException;
  }

  /**
   * 获取上一次计算步骤记录
   */
  public List<String> getLastCalcSteps() {
    return lastCalcSteps;
  }

  /**
   * 获取是否记录运算步骤
   * @return 是否记录运算步骤
   */
  public boolean isRecordStep() {
    return recordStep;
  }

  /**
   * 设置是否记录运算步骤
   * @param recordStep 是否记录运算步骤
   */
  public void setRecordStep(boolean recordStep) {
    this.recordStep = recordStep;
  }

  /**
   * 获取上次计算的算式
   * @return
   */
  public String getLastFormula() {
    return lastFormula;
  }

  /**
   * 获取启用自动科学计数法开始的数值
   */
  public long getScientificNotationMax() { return scientificNotationMax; }

  /**
   * 设置启用自动科学计数法开始的数值
   * @param scientificNotationMax 启用自动科学计数法开始的数值
   */
  public void setScientificNotationMax(long scientificNotationMax) {
    this.scientificNotationMax = scientificNotationMax;
  }

  /**
   * 获取是否启用自动科学计数法显示
   * @return 是否启用自动科学计数法显示
   */
  public boolean isAutoScientificNotation() {
    return autoScientificNotation;
  }

  /**
   * 设置是否启用自动科学计数法显示
   * @param autoScientificNotation 是否启用自动科学计数法显示
   */
  public void setAutoScientificNotation(boolean autoScientificNotation) {
    this.autoScientificNotation = autoScientificNotation;
  }

  //============================
  // 设置和配置
  //============================

  private boolean forceNoNumberCheck = false;
  private long scientificNotationMax = 100000;
  private boolean autoScientificNotation = true;
  private int numberScale = 10;
  private int bcMode = BC_MODE_DEC;
  private boolean lastSuccess = true;
  private boolean lastContainsBinaryConversion = true;
  private Exception lastException = null;
  private List<String> lastCalcSteps = new ArrayList<>();
  private String lastFormula = "";
  private boolean recordStep = false;// 是否记录运算步骤
  private boolean useDegree = false;// 是否使用角度制
  private boolean useModAsPercent = true;// 是否将求余%变成百分号
  private Random random = null;//随机数生成器

  //============================
  // 运算符解析器模块
  //============================

  public static final int OP_TYPE_START = 0;
  public static final int OP_TYPE_END = 1;
  public static final int OP_TYPE_BOTH = 2;

  /**
   * 函数解析器回调
   */
  public interface AutoCalcFunctionActuator {
    /**
     * 函数计算回调
     * @param formula 算式
     * @param formulaBuffer 算式 StringBuilder
     * @param functionName 函数名称
     * @param radians 如果该函数执行器注册了弧度自动转换，此参数传入用户输入的第一个函数参数数值（弧度）
     * @param params 用户输入的函数参数
     * @return 返回计算结果（字符串）
     * @throws AutoCalcException 计算错误时抛出错误
     * @throws AutoCalcInfiniteException 计算结果为无限大时抛出错误
     */
    String onCalcFunction(String formula, StringBuilder formulaBuffer, String functionName, BigDecimal radians, String[] params) throws Exception;
  }

  /**
   * 函数解析器基类
   */
  private class AutoCalcFunctionSolver {
    AutoCalcFunctionActuator actuator;
    String functionName;
    int paramCount;
    boolean castDegree;
    boolean containsBinaryConversion;
    String numberCheckRangeType = "";

    AutoCalcFunctionSolver(AutoCalcFunctionActuator actuator, String functionName, int paramCount,
                           boolean castDegree, boolean containsBinaryConversion, String numberCheckRangeType) {
      this.actuator = actuator;
      this.paramCount = paramCount;
      this.functionName = functionName;
      this.castDegree = castDegree;
      this.containsBinaryConversion = containsBinaryConversion;
      this.numberCheckRangeType = numberCheckRangeType;
    }
  }

  /**
   * 运算符解析器基类
   */
  public class AutoCalcOperatorSolver {

    String name;
    /**
     * 优先级，越大优先级越高
     */
    int priority;
    /**
     * 运算符类型
     * OP_TYPE_START = 0 前缀运算符
     * OP_TYPE_END = 1 后缀运算符
     * OP_TYPE_BOTH = 2 中缀运算符
     *
     * 对于 OP_TYPE_START ，有 sr 和 nr 参数，其他参数为 "NULL"
     * 对于 OP_TYPE_END ，有 sl 和 nl 参数，其他参数为 "NULL"
     * 对于 OP_TYPE_BOTH ，有 sl、nl、sr 和 nr 参数
     */
    int type;
    /**
     * 指定当前解析器要匹配的运算符
     */
    ArrayList<String> checkOpSymbols = new ArrayList<>();
    /**
     * 指定当解析器没有匹配到参数时使用的默认运算参数
     */
    String defaultCalcValue = "NaN";

    /**
     * 创建运算符解析器
     * @param name 名称
     * @param priority 优先级
     * @param type 运算符类型
     */
    public AutoCalcOperatorSolver(String name, int priority, int type, String[] checkOpSymbols) {
      this.name = name;
      this.type = type;
      this.priority = priority;
      this.checkOpSymbols.addAll(Arrays.asList(checkOpSymbols));
      for(String s : checkOpSymbols)
        operators.add(new OperatorInfo(s, type));
    }

    //可被继承的函数

    public void onRemoved() {
      operators.removeAll(checkOpSymbols);
    }
    /**
     * 匹配运算符
     * @param formulaBuffer 算式
     * @return 返回是否匹配成功
     */
    public boolean onPatch(StringBuilder formulaBuffer) {
      return onTestOperator(formulaBuffer);
    }
    /**
     * 运算时匹配运算符
     * @param formulaBuffer 算式
     * @param operatorSymbol 当前运算符
     * @param index 当前运算符位置索引
     * @return 返回是否匹配成功
     */
    public boolean onSolvePreMatch(StringBuilder formulaBuffer, String operatorSymbol, int index) {
      return true;
    }
    /**
     * 测试运算符是否在算式中存在
     * 在默认匹配时调用
     * @param formulaBuffer 算式
     * @return 运算符是否在算式中存在
     */
    public boolean onTestOperator(StringBuilder formulaBuffer) {
      for (String symbol : checkOpSymbols)
        if (formulaBuffer.indexOf(symbol) >= 0)
          return true;
      return false;
    }

    /**
     * 计算运算符
     * @param sl 左边参数（字符串）
     * @param sr 右边参数（字符串）
     * @param nl 左边参数
     * @param nr 右边参数
     * @param operator 当前运算符
     * @return 返回计算结果（字符串）
     * @throws AutoCalcException 计算错误时抛出错误
     * @throws AutoCalcInfiniteException 计算结果为无限大时抛出错误
     */
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      return "NaN";
    }

    //内核处理函数

    void onCalcSolve(StringBuilder formulaBuffer) throws AutoCalcException, AutoCalcInfiniteException {

      int formulaSize = formulaBuffer.length();
      int formulaSolvedInxdex = 0;

      for (int i = 0; i < formulaSize; i++) {

        //寻找运算符
        for (String symbol : checkOpSymbols) {
          //单个字符op
          int symbolSize = symbol.length();
          if (symbolSize == 1) {
            if (i < formulaSize && formulaBuffer.charAt(i) == symbol.charAt(0)) {
              if(calcSolvePreMatch(formulaBuffer, symbol, i))
                continue;
              formulaSolvedInxdex = calcSolveMatchOp(formulaBuffer, i, symbol);
              formulaSize = formulaBuffer.length();
              if(formulaSolvedInxdex >= 0)
                i = formulaSolvedInxdex - 1;
            }
          } else if (symbolSize > 1) {
            //多个字符组成的op
            if (i < formulaSize && formulaBuffer.charAt(i) == symbol.charAt(0)) {
              boolean match = true;
              for (int j = 0; j < symbolSize; j++) {
                if (i + j >= formulaBuffer.length() || formulaBuffer.charAt(i + j) != symbol.charAt(j)) match = false;
              }
              if (match) {
                if (calcSolvePreMatch(formulaBuffer, symbol, i))
                  continue;
                formulaSolvedInxdex = calcSolveMatchOp(formulaBuffer, i, symbol);
                formulaSize = formulaBuffer.length();
                if(formulaSolvedInxdex >= 0)
                  i = formulaSolvedInxdex - 1;
              }
            }
          }
        }

      }
    }

    private int calcSolveMatchOp(StringBuilder formulaBuffer, int i, String symbol) throws AutoCalcException, AutoCalcInfiniteException {

      String formulaLeft;
      String formulaRight;
      int replacedIndex = -1;

      if (type == OP_TYPE_END || type == OP_TYPE_BOTH) formulaLeft = cutNumberDirection(formulaBuffer, i, true);
      else formulaLeft = "NULL";
      if (type == OP_TYPE_START || type == OP_TYPE_BOTH) formulaRight = cutNumberDirection(formulaBuffer, i + symbol.length() - 1, false);
      else formulaRight = "NULL";

      replacedIndex = calcSolveCheckedOperator(formulaBuffer, formulaLeft, formulaRight, symbol);
      if (replacedIndex >= 0) {
        if(recordStep)
          lastCalcSteps.add("(" + formulaLeft + symbol + formulaRight + ")=" + formulaBuffer.toString());
      }

      return replacedIndex;
    }

    private int calcSolveCheckedOperator(StringBuilder formulaBuffer, String formulaLeft, String formulaRight, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      StringBuilder formulaOld = new StringBuilder();
      if(!formulaLeft.equals("NaN") && !formulaLeft.equals("NULL"))
        formulaOld.append(formulaLeft);
      formulaOld.append(operator);
      if(!formulaRight.equals("NaN") && !formulaRight.equals("NULL"))
        formulaOld.append(formulaRight);

      int oldIndex = formulaBuffer.indexOf(formulaOld.toString());
      if (oldIndex >= 0) {
        String result;
        if((type == OP_TYPE_BOTH || type == OP_TYPE_END) && formulaLeft.equals("NaN")) result = "NaN";
        else if((type == OP_TYPE_BOTH || type == OP_TYPE_START) && formulaRight.equals("NaN")) result = "NaN";
        else result = calcOperatorSingle(formulaLeft, formulaRight, operator);

        formulaBuffer.replace(oldIndex, oldIndex + formulaOld.length(), result);

        return oldIndex + result.length();
      }
      return -1;
    }

    private boolean calcSolvePreMatch(StringBuilder formulaBuffer, String operatorSymbol, int index) {
      boolean rs;

      if(type == OP_TYPE_START) {
        rs =  index == 0 || isOperatorOrParentheses(formulaBuffer.charAt(index - 1));
      } else if(type == OP_TYPE_END) {
        rs =  index == formulaBuffer.length() - 1 || isOperatorOrParentheses(formulaBuffer.charAt(index + 1));
      } else rs = type == OP_TYPE_BOTH
              && (
              (index != 0 && !isOperatorOrParentheses(formulaBuffer.charAt(index - 1)))
                      || (index != formulaBuffer.length() - 1 && !isOperatorOrParentheses(formulaBuffer.charAt(index + 1)))
      );
      if(rs) rs = onSolvePreMatch(formulaBuffer, operatorSymbol, index);

      return !rs;
    }

    private String cutNumberDirection(StringBuilder formulaBuffer, int start, boolean left) {
      boolean oneAddSubNumberFind = false;
      int startIndex = 0;
      int endIndex = 0;
      int formulaSize = formulaBuffer.length();
      if (left) {
        endIndex = start;
        for (start-- ; start >= 0; start--) {
          if (isNumChar(formulaBuffer.charAt(start))) oneAddSubNumberFind = true;
          if (
                  (!oneAddSubNumberFind && isOperatorWithoutAddSub(formulaBuffer.charAt(start)))
                          || (oneAddSubNumberFind && isOperator(formulaBuffer.charAt(start)) && start != 0)
          ) {
            startIndex = start + 1;
            break;
          }
        }
      } else {
        startIndex = start + 1;
        for (start++; start < formulaSize; start++) {
          if (isNumChar(formulaBuffer.charAt(start))) oneAddSubNumberFind = true;
          if (
                  (!oneAddSubNumberFind && isOperatorWithoutAddSub(formulaBuffer.charAt(start)))
                          || (oneAddSubNumberFind && isOperator(formulaBuffer.charAt(start)))
          ) {
            endIndex = start;
            break;
          }
          if(start == formulaSize - 1)
            endIndex = formulaSize;
        }
      }
      if (startIndex >= 0 && startIndex < endIndex && endIndex <= formulaSize) {
        //if(formulaBuffer.charAt(endIndex) == '+' || formulaBuffer.charAt(endIndex) == '-')
        //  return formulaBuffer.substring(startIndex, endIndex - 1);
        return formulaBuffer.substring(startIndex, endIndex);
      }
      return defaultCalcValue;
    }

    /**
     * 计算单个运算符
     *
     * @param sl       左
     * @param sr       右
     * @param operator 运算符
     * @return 返回已计算结果
     * @throws AutoCalcException 计算发生错误时抛出此异常
     */
    private String calcOperatorSingle(String sl, String sr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      // 优先级 由上到下

      // 是NaN说明是第一次运算(并且不是单向运算符)，直接返回
      if ("NaN".equals(sl) && this.type != OP_TYPE_START)
        return sr;

      //数字转换
      BigDecimal nl = BigDecimal.ZERO, nr = BigDecimal.ZERO;
      if (!"NaN".equals(sl) && !"NULL".equals(sl)) {
        if (tools.isNumber(sl)) nl = tools.strToNumber(sl);
        else if (!"".equals(sl)) throw new AutoCalcException("未定义的符号：" + sl);
      }
      if (!"NaN".equals(sr) && !"NULL".equals(sr)) {
        if (tools.isNumber(sr)) nr = tools.strToNumber(sr);
        else if (!"".equals(sr)) throw new AutoCalcException("未定义的符号：" + sr);
      }

      return this.onCalcOperator(sl, sr, nl, nr, operator);
    }
    /**
     * 检查算式中是否有比当前优先级更高的运算符
     * @param formulaBuffer 算式
     */
    public boolean hasMoreAdvancedOperator(StringBuilder formulaBuffer) {
      int index = operatorSolvers.indexOf(this);
      for (int i = index - 1; i >= 0; i--) {
        if (operatorSolvers.get(i).onTestOperator(formulaBuffer))
          return true;
      }
      return false;
    }
  }


  /*
   * 运算符解析器模块
   *
   * 运算解析器负责解析算式中的运算符以及其他运算方式
   * 并按次序进行解析运算
   *
   */

  /**
   * 注册运算符解析器
   *
   * @param solver 解析器
   */
  public void addCalcOperatorSolver(AutoCalcOperatorSolver solver) {
    if (!isCalcOperatorSolverExists(solver.name)) {
      operatorSolvers.add(solver);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        operatorSolvers.sort((o1, o2) -> -Integer.compare(o1.priority, o2.priority));
      }
    }
  }

  /**
   * 查看运算符解析器是否注册
   *
   * @param solverName 解析器名称
   * @return 是否注册
   */
  public boolean isCalcOperatorSolverExists(String solverName) {
    for (AutoCalcOperatorSolver solver : operatorSolvers)
      if (solver.name.equals(solverName)) return true;
    return false;
  }

  /**
   * 取消注册运算符解析器
   *
   * @param solverName 解析器名称
   */
  public void removeCalcOperatorSolver(String solverName) {
    for (int i = operatorSolvers.size() - 1; i >= 0; i--) {
      if (operatorSolvers.get(i).name.equals(solverName))
        operatorSolvers.get(i).onRemoved();
      operatorSolvers.remove(i);
    }
  }

  /**
   * 注册函数解析器
   *
   * @param functionName     函数名称
   * @param functionActuator 解析器回调
   * @param paramCount       函数需要的参数
   * @param castDegree       是否自动转换参数为角度制
   * @param containsBinaryConversion 是否
   */
  public void addCalcFunctionActuatorSolver(String functionName, int paramCount, boolean castDegree,
                                            boolean containsBinaryConversion,
                                            AutoCalcFunctionActuator functionActuator, String numberCheckRangeType) {
    if (!isCalcFunctionActuatorExists(functionName)) {
      functionSolvers.add(new AutoCalcFunctionSolver(functionActuator, functionName, paramCount,
              castDegree, containsBinaryConversion, numberCheckRangeType));
    }
  }

  /**
   * 函数解析器是否注册
   *
   * @param functionName 函数名称
   * @return 是否注册
   */
  public boolean isCalcFunctionActuatorExists(String functionName) {
    for (AutoCalcFunctionSolver solver : functionSolvers)
      if (solver.functionName.equals(functionName)) return true;
    return false;
  }

  /**
   * 取消注册函数解析器
   *
   * @param functionName 函数名称
   */
  public void removeCalcFunctionActuator(String functionName) {
    for (int i = functionSolvers.size() - 1; i >= 0; i--) {
      if (functionSolvers.get(i).functionName.equals(functionName))
        functionSolvers.remove(i);
    }
  }

  /**
   * 注册替换常量
   *
   * @param symbol 解析器
   * @param value  常量值
   */
  public void addCalcConstants(String symbol, Double value) {
    if (!isCalcOperatorSolverExists(symbol))
      constants.put(symbol, value);
  }

  /**
   * 查看替换常量是否注册
   *
   * @param symbol 常量名称
   * @return 是否注册
   */
  public boolean isCalcConstantsExists(String symbol) {
    return constants.containsKey(symbol);
  }

  /**
   * 取消注册替换常量
   *
   * @param symbol 常量名称
   */
  public void removeCalcConstants(String symbol) {
    constants.remove(symbol);
  }

  //============================
  //所有部件池
  //============================

  /**
   * 运算符解析器数组
   * <p>
   * 按优先级从大到小排序
   */
  private ArrayList<AutoCalcOperatorSolver> operatorSolvers = new ArrayList<>();
  /**
   * 运算符解析器数组
   * <p>
   * 按优先级从大到小排序
   */
  private ArrayList<AutoCalcFunctionSolver> functionSolvers = new ArrayList<>();
  /**
   * 运算符解析器数组
   * <p>
   * 按优先级从大到小排序
   */
  private Map<String, Double> constants = new HashMap<>();

  private class OperatorInfo {
    String operator;
    int type;

    public OperatorInfo(String operator, int type) {
      this.operator = operator;
      this.type = type;
    }
  }
  /**
   * 所有运算符
   */
  private List<OperatorInfo> operators = new ArrayList<>();

  /**
   * 计算主函数
   */
  private void calcCore(StringBuilder formulaBuffer, boolean isFirst) throws Exception {
    if (formulaBuffer.length() == 0) return;
    if (formulaBuffer.indexOf("∞") > -1) {
      formulaBuffer.delete(0, formulaBuffer.length());
      formulaBuffer.append("∞");
      return;
    }
    if (formulaBuffer.indexOf("(") > -1) calcParenthesis(formulaBuffer);// 先计算括号
    if (formulaBuffer.indexOf(",") > -1) // 识别为函数参数
    {
      String[] ss = formulaBuffer.toString().split(",");
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < ss.length; i++)// 参数逐一分解
      {
        StringBuilder arg = new StringBuilder(ss[i]);

        calcCore(arg, false);
        sb.append(arg);

        if (i != ss.length - 1) sb.append(",");
      }

      formulaBuffer.delete(0, formulaBuffer.length());
      formulaBuffer.append(sb);

    } else {
      calcConstants(formulaBuffer); // 再替换常量

      //匹配运算符，优先级从高到低
      boolean formulaPatched = false;
      AutoCalcOperatorSolver solver;
      for (AutoCalcOperatorSolver operatorSolver : operatorSolvers) {
        solver = operatorSolver;
        if (solver.onPatch(formulaBuffer)) {
          solver.onCalcSolve(formulaBuffer);
          formulaPatched = true;
        }
      }

      //无法处理，抛出异常
      if (!forceNoNumberCheck && !formulaPatched && !tools.isNumber(formulaBuffer) && formulaBuffer.length() != 0)
        throw new AutoCalcException("错误的输入：" + formulaBuffer.toString());//

      removeContinuousZero(formulaBuffer);
    }
    if (isFirst && tools.isNumber(formulaBuffer) && !lastContainsBinaryConversion) {
      autoBinaryConversion(formulaBuffer);
      removeContinuousZero(formulaBuffer);
    }
  }

  /**
   * 把字符串中含有的常量转换成数字
   * @param formulaBuffer 算式
   */
  private void calcConstants(StringBuilder formulaBuffer) throws AutoCalcException, AutoCalcInfiniteException {
    //常量池
    Set<String> constantsKeySet = constants.keySet();
    for (String symbol : constantsKeySet) {
      String symbolText = tools.numberToStr(constants.get(symbol));
      int symbolLength = symbol.length();
      for (int i = 0; i < formulaBuffer.length(); i++) {

        if(symbolLength == 1) {
          if(formulaBuffer.charAt(i) == symbol.charAt(0)) {
            if(i >= 1 && isNumChar(formulaBuffer.charAt(i - 1))) continue;
            if(i < formulaBuffer.length() - 1 && isNumChar(formulaBuffer.charAt(i + 1))) continue;

            formulaBuffer.replace(i, i + symbolLength, symbolText);
          }
        }else if(symbolLength > 1) {
          if(formulaBuffer.charAt(i) == symbol.charAt(0)) {
            boolean matched = true;
            for (int j = 0; j < symbolLength; j++) {
              if (i + j < formulaBuffer.length() && formulaBuffer.charAt(i + j) != symbol.charAt(j)) {
                matched = false;
                break;
              }
            }
            if (matched) {

              if (i >= 1 && isNumChar(formulaBuffer.charAt(i - 1))) continue;
              if (i + symbolLength < formulaBuffer.length() - 1 && isNumChar(formulaBuffer.charAt(i + symbolLength + 1)))
                continue;

              formulaBuffer.replace(i, i + symbolLength, symbolText);

            }
          }
        }
      }
    }
  }

  /**
   * 处理函数
   *
   * @param formulaBuffer 算式
   * @param functionName  函数名称
   * @return 返回已计算的结果
   * @throws AutoCalcException         计算发生错误时抛出此异常
   * @throws AutoCalcInfiniteException 当计算为无限大时抛出此异常
   */
  private void calcFunction(StringBuilder formulaBuffer, String functionName) throws Exception {

    String formula = formulaBuffer.toString();

    for (AutoCalcFunctionSolver solver : functionSolvers) {
      if (solver.functionName.equals(functionName)) {

        String[] params = null;

        //检查参数
        if (solver.paramCount >= 1) {
          if (formula.contains(",")) params = formula.split(",");
          if (solver.paramCount > 1 && (params == null || params.length != solver.paramCount))
            throw new AutoCalcException("函数：" + functionName + " 需要 " + solver.paramCount + " 个参数");
        }

        //处理某些弧度函数
        BigDecimal radians = BigDecimal.ZERO;
        if (solver.castDegree) {
          try {
            // 根据设置转换文字（角度/弧度）为弧度
            radians = useDegree ? math.d2g(BigDecimal.valueOf(Double.valueOf(formula))) : BigDecimal.valueOf(Double.valueOf(formula));
          } catch (Exception e) {
            throw new AutoCalcException("未知符号：" + formula);
          }
        }

        if(solver.containsBinaryConversion)
          lastContainsBinaryConversion = true;

        String result = solver.actuator.onCalcFunction(formula, formulaBuffer, functionName, radians, params);

        //Step
        if(recordStep) lastCalcSteps.add(functionName + "(" + formula + ")=" + result);

        formulaBuffer.delete(0, formulaBuffer.length());
        formulaBuffer.append(result);

        return;
      }
    }

    BigDecimal nr;
    if (tools.isNumber(functionName)) {

      nr = tools.strToNumber(functionName);
      String result = tools.numberToStr(nr.multiply(tools.strToNumber(formula)));

      lastCalcSteps.add(formulaBuffer.toString() + "=" +result);

      formulaBuffer.delete(0, formulaBuffer.length());
      formulaBuffer.append(result);

    } else throw new AutoCalcException("未知函数：" + functionName);
  }

  /**
   * 递归处理算式中的括号
   *
   * @param formulaBuffer 算式
   * @throws AutoCalcException         计算发生错误时抛出此异常
   * @throws AutoCalcInfiniteException 当计算为无限大时抛出此异常
   */
  private void calcParenthesis(StringBuilder formulaBuffer) throws Exception {
    //
    // 先从外到里
    boolean qfinded = false;
    int i, qc = 0, qstart = 0;
    for (i = 0; i < formulaBuffer.length(); i++) {
      // 括号计数
      //
      if (formulaBuffer.charAt(i) == '(') {
        qc++;
        if (!qfinded)
          qfinded = true;
        if (qc == 1)
          qstart = i;
      } else if (formulaBuffer.charAt(i) == ')')
        qc--;
      if (qc == 0 && qfinded) {
        // 括号结尾
        qfinded = false;

        String formulaInn = formulaBuffer.substring(qstart + 1, i);
        // 找括号前面是否有函数名
        String frontName = "";
        if (qstart > 0) {
          boolean frontNameSearched = false;
          for (int j = qstart - 1; j >= 0; j--) {
            if (isOperatorOrParentheses(formulaBuffer.charAt(j))) {
              frontName = formulaBuffer.substring(j + 1, qstart);
              frontNameSearched = true;
              break;
            }
          }
          if(!frontNameSearched) {
            frontName = formulaBuffer.substring(0, qstart);
          }
        }


        // 递归将括号中的算式算掉
        StringBuilder resultPartBuilder = new StringBuilder(formulaInn);
        calcCore(resultPartBuilder, false);

        // 如果有函数名则计算函数
        if (!"".equals(frontName)) {
          calcFunction(resultPartBuilder, frontName);

          // 结果替换到原算式中
          String resultWithParenthesis = frontName + "(";
          int ixx = formulaBuffer.indexOf(resultWithParenthesis);
          formulaBuffer.delete(ixx, ixx + resultWithParenthesis.length());
        } else {
          // 删除第一个(
          int ixx = formulaBuffer.indexOf("(");
          formulaBuffer.delete(ixx, ixx + 1);
        }

        String formulaWithParenthesis = formulaInn + ")";
        int index = formulaBuffer.indexOf(formulaWithParenthesis);
        if (index > -1) {
          formulaBuffer.delete(index, index + formulaWithParenthesis.length());
          formulaBuffer.insert(index, resultPartBuilder);
          //已发生替换动作，现在需要重新搜索，因为字符串长度已经变化
          i = 0;
        }
      }
    }
  }

  /**
   * 去除多余的 0
   * @param stringBuilder
   */
  private void removeContinuousZero(StringBuilder stringBuilder) {
    int pointPos = stringBuilder.indexOf("."), lastNoZeroPos = 0;
    if(pointPos > 0) {
      boolean findNotZero = false;
      for (int si = pointPos + 1, sc = stringBuilder.length(); si < sc; si++) {
        if(stringBuilder.charAt(si) != '0') {
          lastNoZeroPos = si;
          findNotZero = true;
        }
      }
      if(lastNoZeroPos > pointPos && lastNoZeroPos < stringBuilder.length() - 2) stringBuilder.delete(lastNoZeroPos + 2, stringBuilder.length());
      else if(!findNotZero && pointPos < stringBuilder.length()) stringBuilder.delete(pointPos, stringBuilder.length());
    }
  }

  /**
   * 自动转换进制
   * @param stringBuilder 数字
   */
  private void autoBinaryConversion(StringBuilder stringBuilder) throws AutoCalcException {
    String radix = tools.getNumberStrRadix(stringBuilder.toString());
    String fixNumber = null;

    if((!radix.equals("d") && bcMode == BC_MODE_DEC)
            || (!radix.equals("b") && bcMode == BC_MODE_BIN)
            || (!radix.equals("o") && bcMode == BC_MODE_OCT)
            || (!radix.equals("h") && bcMode == BC_MODE_HEX)) {
      fixNumber = tools.numberToStr(tools.strToNumber(stringBuilder));
    }

    if(fixNumber != null) {
      stringBuilder.delete(0, stringBuilder.length());
      stringBuilder.append(fixNumber);
    }
  }

  //==============================

  //初始化

  private void initAllOperatorSolver() {

    //这里定义所有运算符运算

    addCalcOperatorSolver(new AddSubOperatorSolver());
    addCalcOperatorSolver(new MulDivOperatorSolver());
    addCalcOperatorSolver(new PowOperatorSolver());
    addCalcOperatorSolver(new PercentOperatorSolver());
    addCalcOperatorSolver(new SquareRootAndCubeRootOperatorSolver());
    addCalcOperatorSolver(new ModOperatorSolver());
    addCalcOperatorSolver(new FactorialOperatorSolver());
    addCalcOperatorSolver(new LogicOperatorSolver());
    addCalcOperatorSolver(new LogicNotOperatorSolver());
  }

  private void initAllFunctionSolver() {

    //这里定义所有函数运算

    addCalcFunctionActuatorSolver("sin", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.sin(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("cos", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.cos(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("tan", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.tan(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("sinh", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.sinh(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("cosh", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.cosh(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("tanh", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.tanh(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("arcsin", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.asin(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("arccos", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.acos(radians.doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("arctan", 1, true, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.atan(radians.doubleValue())),
            "double");


    addCalcFunctionActuatorSolver("lg", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.log10(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("ln", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.log(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("exp", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.exp(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("expm1", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.expm1(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("log1p", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.log1p(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("sqrt", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.sqrt(tools.strToNumber(formula).doubleValue())),
            "double");
    addCalcFunctionActuatorSolver("cbrt", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.cbrt(tools.strToNumber(formula).doubleValue())),
            "double");

    addCalcFunctionActuatorSolver("pow", 2, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              BigDecimal radix = tools.strToNumber(params[1]);
              if(radix.compareTo(BigDecimal.ZERO) <= 0 || radix.compareTo(BigDecimal.valueOf(32)) > 0)
                throw new AutoCalcInfiniteException();
              return tools.numberToStr(tools.strToNumber(params[0]).pow(radix.intValue()));
            },
            "double");
    addCalcFunctionActuatorSolver("hypot", 2, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(Math.hypot(Double.valueOf(params[0]), Double.valueOf(params[1]))),
            "double");

    addCalcFunctionActuatorSolver("fact", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(math.fact(tools.strToNumber(formula))),
            "");
    addCalcFunctionActuatorSolver("cell", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(tools.strToNumber(formula).setScale(0, BigDecimal.ROUND_UP)),
            "");
    addCalcFunctionActuatorSolver("floor", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(tools.strToNumber(formula).setScale(0, BigDecimal.ROUND_DOWN)),
            "");
    addCalcFunctionActuatorSolver("fix", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(tools.strToNumber(formula).setScale(0, BigDecimal.ROUND_HALF_UP)),
            "");

    addCalcFunctionActuatorSolver("g2d", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(math.g2d(BigDecimal.valueOf(Double.valueOf(formula)))),
            "double");
    addCalcFunctionActuatorSolver("d2g", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(math.d2g(BigDecimal.valueOf(Double.valueOf(formula)))),
            "double");

    addCalcFunctionActuatorSolver("hex", 1, false, true,
            (formula, formulaBuffer, functionName, radians, params) -> Long.toHexString(tools.strToNumber(formula).longValue()) + "h",
            "long");
    addCalcFunctionActuatorSolver("dec", 1, false, true,
            (formula, formulaBuffer, functionName, radians, params) -> Long.toString(tools.strToNumber(formula).longValue()), "");
    addCalcFunctionActuatorSolver("bin", 1, false, true,
            (formula, formulaBuffer, functionName, radians, params) -> Long.toBinaryString(tools.strToNumber(formula).longValue()) + "b",
            "long");
    addCalcFunctionActuatorSolver("oct", 1, false, true,
            (formula, formulaBuffer, functionName, radians, params) -> Long.toOctalString(tools.strToNumber(formula).longValue()) + "o",
            "long");

    addCalcFunctionActuatorSolver("negate", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              radians = tools.strToNumber(formula);
              int i = radians.compareTo(BigDecimal.ZERO);
              if (i == 0) return "0";
              else {
                radians = radians.negate();
                return tools.numberToStr(radians);
              }
            }, "");
    addCalcFunctionActuatorSolver("abs", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              radians = tools.strToNumber(formula);
              int i = radians.compareTo(BigDecimal.ZERO);
              if (i >= 0) return tools.numberToStr(radians);
              else {
                radians = radians.negate();
                return tools.numberToStr(radians);
              }
            },"");

    addCalcFunctionActuatorSolver("radix", 2, false, true,
            (formula, formulaBuffer, functionName, radians, params) -> {
              BigDecimal radix = tools.strToNumber(params[1]);
              if(radix.compareTo(BigDecimal.ZERO) <= 0 || radix.compareTo(BigDecimal.valueOf(32)) > 0)
                throw new AutoCalcException("进制超出范围");
              return Long.toString(tools.strToNumber(params[0]).longValue(), radix.intValue());
            },
            "long");

    addCalcFunctionActuatorSolver("log", 2, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> tools.numberToStr(math.log(tools.strToNumber(params[0]), tools.strToNumber(params[1]))),
            "double");

    addCalcFunctionActuatorSolver("rand", 0, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              if (random == null) random = new Random();
              return String.valueOf(random.nextDouble());
            }, "");
    addCalcFunctionActuatorSolver("rnd", 2, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              if (random == null) random = new Random();
              return String.valueOf(math.rand(Long.valueOf(params[0]), Long.valueOf(params[1])));
            },"long");

    addCalcFunctionActuatorSolver("ver", 0, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> "2.0",
            "");

    addCalcFunctionActuatorSolver("capitalNumber", 1, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              forceNoNumberCheck = true;
              return ConvertUpMoney.toChinese(formula);
            }, "double");

    addCalcFunctionActuatorSolver("test", 0, false, false,
            (formula, formulaBuffer, functionName, radians, params) -> {
              throw new Exception("test Exception");
            },
            "");
  }

  private void initAllConstants() {

    //这里定义所有常量

    addCalcConstants("e", Math.E);
    addCalcConstants("е", Math.E);
    addCalcConstants("pi", Math.PI);
    addCalcConstants("π", Math.PI);
  }


  //判断函数

  public boolean containsOperator(String s) {
    for (OperatorInfo operator : operators)
      if(s.contains(operator.operator)) return true;
    return false;
  }
  public boolean isOperator(String s) {
    return operators.contains(s);
  }
  public boolean isOperator(char s) {
    for (OperatorInfo operator : operators)
      if (operator.operator.length() == 1 && operator.operator.charAt(0) == s)
        return true;
    return false;
  }
  public boolean isOperator(char s, int opType) {
    for (OperatorInfo operator : operators)
      if (operator.operator.length() == 1 && operator.operator.charAt(0) == s) {
        if (operator.type == opType)
          return true;
        break;
      }
    return false;
  }
  public boolean isOperatorWithoutAddSub(char s) {
    return s != '-' && s != '+' && isOperator(s);
  }
  public boolean isOperatorOrParentheses(char s) {
    return (s == '(' || s == ')') || isOperator(s);
  }
  public boolean isNumChar(char s) {
    return ((s >= '0' && s <= '9') || (s >= 'a' && s <= 'f') || (s >= 'A' && s <= 'F') || s == 'x');
  }
  public boolean isNoNumChar(char s) {
    return (s == ' ' || !isNumChar(s));
  }

  //==============================

  //所有运算符解析器

  private class AddSubOperatorSolver extends AutoCalcOperatorSolver {

    AddSubOperatorSolver() {
      super("AddSub", 4, OP_TYPE_BOTH, new String[] { "+", "-" });
      defaultCalcValue = "0";
    }

    @Override
    public boolean onPatch(StringBuilder formulaBuffer) {
      return formulaBuffer.indexOf("+") >= 0 || (formulaBuffer.indexOf("-") >= 0 && checkDiv(formulaBuffer));
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "+": return tools.numberToStr(nl.add(nr));
        case "-": return tools.numberToStr(nl.subtract(nr));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }

    private boolean checkDiv(StringBuilder s) {
      int subtractCount = 0;
      char ch, ch_before = 0;
      for (int i = 0; i < s.length(); i++) {
        ch = s.charAt(i);
        if (i > 0) ch_before = s.charAt(i - 1);
        if (ch == '-') subtractCount++;
        if (ch == '-' && i > 0 && isOperator(ch_before) && ch_before != '-' && ch_before != '+')
          return false;
      }
      return (s.charAt(0) != '-' || subtractCount > 1);
    }

  }
  private class MulDivOperatorSolver extends AutoCalcOperatorSolver {

    MulDivOperatorSolver() {
      super("MulDiv", 7, OP_TYPE_BOTH, new String[] { "*", "×", "/", "÷" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "*":
        case "×":
          return tools.numberToStr(nl.multiply(nr));
        case "/":
        case "÷":
          if (nr.compareTo(BigDecimal.ZERO) == 0) throw new AutoCalcException("不能除以0");
          return tools.numberToStr(nl.divide(nr, numberScale, BigDecimal.ROUND_HALF_UP));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }
  }

  private class FactorialOperatorSolver extends AutoCalcOperatorSolver {

    FactorialOperatorSolver() {
      super("Factorial", 11, OP_TYPE_END, new String[] { "!" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "!":
          return tools.numberToStr(math.fact(nl));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }
  }
  private class PowOperatorSolver extends AutoCalcOperatorSolver {

    PowOperatorSolver() {
      super("Pow", 8, OP_TYPE_BOTH, new String[] { "^" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "^":
          tools.checkNumberRangeAndThrow(nr, "int");
          int ap = nr.intValue();
          if(ap > 10000) throw new AutoCalcInfiniteException();
          return tools.numberToStr(ap >= 0 ? nl.pow(ap) : BigDecimal.ONE.divide(nl.pow(-ap), numberScale, BigDecimal.ROUND_HALF_UP));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }
  }
  private class PercentOperatorSolver extends AutoCalcOperatorSolver {

    PercentOperatorSolver() {
      super("Percent", 9, OP_TYPE_END, new String[] { "%" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "%":
          return tools.numberToStr(nl.divide(BigDecimal.valueOf(100), numberScale, BigDecimal.ROUND_HALF_UP));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }

    @Override
    public boolean onPatch(StringBuilder formula) {
      return super.onPatch(formula) && useModAsPercent;
    }
  }
  private class SquareRootAndCubeRootOperatorSolver extends AutoCalcOperatorSolver {

    SquareRootAndCubeRootOperatorSolver() {
      super("SquareRootAndCubeRoot", 10, OP_TYPE_START, new String[] { "√", "∛" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      tools.checkNumberRangeAndThrow(nr, "double");
      switch (operator) {
        case "√":
          return tools.numberToStr(Math.sqrt(nr.doubleValue()));
        case "∛":
          return tools.numberToStr(Math.cbrt(nr.doubleValue()));
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }
  }
  private class ModOperatorSolver extends AutoCalcOperatorSolver {

    ModOperatorSolver() {
      super("Mod", 7, OP_TYPE_BOTH, new String[] { "%", "mod" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "MOD":
        case "mod":
          if (nr.compareTo(BigDecimal.ZERO) == 0) throw new AutoCalcException("不能除以0");
          return tools.numberToStr(nl.divideAndRemainder(nr)[1]);
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }

    @Override
    public boolean onPatch(StringBuilder formula) {
      return super.onPatch(formula) && !useModAsPercent;
    }

  }
  private class LogicOperatorSolver extends AutoCalcOperatorSolver {

    LogicOperatorSolver() {
      super("Logic", 5, OP_TYPE_BOTH, new String[] { "and", "or", "xor", "<<", ">>", ">>>" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcInfiniteException {

      tools.checkNumberRangeAndThrow(nl, "long");
      tools.checkNumberRangeAndThrow(nr, "long");

      long result = 0;
      switch (operator) {
        case "and": result = nl.longValue() & nr.longValue(); break;
        case "or": result = nl.longValue() | nr.longValue(); break;
        case "xor": result = nl.longValue() ^ nr.longValue(); break;
        case "<<": result = nl.longValue() << nr.longValue(); break;
        case ">>": result = nl.longValue() >> nr.longValue(); break;
        case ">>>": result = nl.longValue() >>> nr.longValue(); break;

      }
      return tools.numberToStr(result);
    }

  }
  private class LogicNotOperatorSolver extends AutoCalcOperatorSolver {

    LogicNotOperatorSolver() {
      super("LogicNot", 7, OP_TYPE_START, new String[] { "not", "~" });
    }

    @Override
    public String onCalcOperator(String sl, String sr, BigDecimal nl, BigDecimal nr, String operator) throws AutoCalcException, AutoCalcInfiniteException {
      switch (operator) {
        case "~":
        case "not":
          tools.checkNumberRangeAndThrow(nr, "long");
          return tools.numberToStr(~nr.longValue());
      }
      return super.onCalcOperator(sl, sr, nl, nr, operator);
    }


  }

}