package com.dreamfish.com.autocalc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类 默认使用 "yyyy-MM-dd HH:mm:ss" 格式化日期
 *
 */
public final class DateUtils {
  /**
   * 英文简写（默认）如：2010-12-01
   */
  public static String FORMAT_SHORT = "yyyy-MM-dd";
  /**
   * 英文全称 如：2010-12-01 23:15:06
   */
  public static String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";
  /**
   * 精确到毫秒的完整时间 如：yyyy-MM-dd HH:mm:ss.S
   */
  public static String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.S";
  /**
   * 中文简写 如：2010年12月01日
   */
  public static String FORMAT_SHORT_CN = "yyyy 年 MM 月 dd 日";
  /**
   * 中文全称 如：2010年12月01日 23时15分06秒
   */
  public static String FORMAT_LONG_CN = "yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒";
  public static String FORMAT_LONG_CN_FOR_TICK = "y 年 M 月 d 天 H 时 m 分 s 秒";
  /**
   * 精确到毫秒的完整中文时间
   */
  public static String FORMAT_FULL_CN = "yyyy年MM月dd日  HH时mm分ss秒SSS毫秒";

  /**
   * 获得默认的 date pattern
   */
  public static String getDatePattern() {
    return FORMAT_LONG;
  }

  /**
   * 根据预设格式返回当前日期
   *
   * @return
   */
  public static String getNow() {
    return format(new Date());
  }

  /**
   * 根据用户格式返回当前日期
   *
   * @param format
   * @return
   */
  public static String getNow(String format) {
    return format(new Date(), format);
  }

  /**
   * 使用预设格式格式化日期
   *
   * @param date
   * @return
   */
  public static String format(Date date) {
    return format(date, getDatePattern());
  }

  /**
   * 使用用户格式格式化日期
   *
   * @param date
   *            日期
   * @param pattern
   *            日期格式
   * @return
   */
  public static String format(Date date, String pattern) {
    String returnValue = "";
    if (date != null) {
      SimpleDateFormat df = new SimpleDateFormat(pattern);
      returnValue = df.format(date);
    }
    return (returnValue);
  }

  /**
   * 使用预设格式提取字符串日期
   *
   * @param strDate
   *            日期字符串
   * @return
   */
  public static Date parse(String strDate) {
    return parse(strDate, getDatePattern());
  }

  /**
   * 使用用户格式提取字符串日期
   *
   * @param strDate
   *            日期字符串
   * @param pattern
   *            日期格式
   * @return
   */
  public static Date parse(String strDate, String pattern) {
    SimpleDateFormat df = new SimpleDateFormat(pattern);
    try {
      return df.parse(strDate);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 在日期上增加数个整月
   *
   * @param date
   *            日期
   * @param n
   *            要增加的月数
   * @return
   */
  public static Date addMonth(Date date, int n) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MONTH, n);
    return cal.getTime();
  }

  /**
   * 在日期上增加天数
   *
   * @param date
   *            日期
   * @param n
   *            要增加的天数
   * @return
   */
  public static Date addDay(Date date, int n) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, n);
    return cal.getTime();
  }

  /**
   * 获取时间戳
   */
  public static String getTimeString() {
    SimpleDateFormat df = new SimpleDateFormat(FORMAT_FULL);
    Calendar calendar = Calendar.getInstance();
    return df.format(calendar.getTime());
  }

  /**
   * 获取日期年份
   *
   * @param date
   *            日期
   * @return
   */
  public static String getYear(Date date) {
    return format(date).substring(0, 4);
  }

  /**
   * 按默认格式的字符串距离今天的天数
   *
   * @param date
   *            日期字符串
   * @return
   */
  public static int countDays(String date) {
    long t = Calendar.getInstance().getTime().getTime();
    Calendar c = Calendar.getInstance();
    c.setTime(parse(date));
    long t1 = c.getTime().getTime();
    return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
  }

  /**
   * 计算日期距离今天的天数
   *
   * @param date
   *            日期字符串
   * @return
   */
  public static int countDays(Date date) {
    long t = Calendar.getInstance().getTime().getTime();
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    long t1 = c.getTime().getTime();
    return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
  }

  /**
   * 计算两个日期相差的天数
   */
  public static int countDays(Date dateStart, Date dateEnd) {
    Calendar calendar1 = Calendar.getInstance();
    calendar1.setTime(dateStart);
    Calendar calendar2 = Calendar.getInstance();
    calendar2.setTime(dateEnd);

    int day1 = calendar1.get(Calendar.DAY_OF_YEAR);
    int day2 = calendar2.get(Calendar.DAY_OF_YEAR);
    int year1 = calendar1.get(Calendar.YEAR);
    int year2 = calendar2.get(Calendar.YEAR);

    if (year1 != year2)  //不同年
    {
      int timeDistance = 0;
      for (int i = year1 ; i < year2 ;i++){ //闰年
        if (getIsLunarYear(i)){
          timeDistance += 366;
        }else { // 不是闰年
          timeDistance += 365;
        }
      }
      return  timeDistance + (day2-day1);
    }else{// 同年
      return day2-day1;
    }
  }

  /**
   * 按用户格式字符串距离今天的天数
   *
   * @param date
   *            日期字符串
   * @param format
   *            日期格式
   * @return
   */
  public static int countDays(String date, String format) {
    long t = Calendar.getInstance().getTime().getTime();
    Calendar c = Calendar.getInstance();
    c.setTime(parse(date, format));
    long t1 = c.getTime().getTime();
    return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
  }

  /**
   * 获取年是不是闰年
   *
   * @param year 日期字符串
   */
  public static boolean getIsLunarYear(int year) {
    return year %4 == 0 && year % 100 != 0||year % 400 == 0;
  }
  /**
   * 获取月份天数
   *
   * @param month 月份
   */
  public static int getDayOfMonth(int month, int year) {
    switch (month) {
      case 1:
      case 3:
      case 5:
      case 7:
      case 8:
      case 10:
      case 12:
        return 31;
      case 4:
      case 6:
      case 9:
      case 11:
        return 31;
      case 2:
        return getIsLunarYear(year) ? 29 : 28;
    }
    return 0;
  }
}