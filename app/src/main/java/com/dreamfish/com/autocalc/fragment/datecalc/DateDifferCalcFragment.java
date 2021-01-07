package com.dreamfish.com.autocalc.fragment.datecalc;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DateDifferCalcFragment extends Fragment {

  public static Fragment newInstance(){
    return new DateDifferCalcFragment();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_page_date_diff_calc, null);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    resources = this.getResources();

    initStrings();
    initControls(view);
  }

  private Resources resources;

  private TimePickerView pvDateStart;
  private TimePickerView pvDateEnd;
  private Button btn_start_date;
  private Button btn_end_date;
  private TextView text_result_date;

  private void updateTimeStart() {
    btn_start_date.setText(DateUtils.format(choosedTimeStart, DateUtils.FORMAT_SHORT_CN));
  }
  private void updateTimeEnd() {
    btn_end_date.setText(DateUtils.format(choosedTimeEnd, DateUtils.FORMAT_SHORT_CN));
  }

  private String text_all;
  private String text_month;
  private String text_day;
  private String text_year;
  private String text_same_day;

  private void initControls(View view) {

    pvDateStart = new TimePickerBuilder(this.getContext(),
            (date, v) -> {
              choosedTimeStart = date;
              updateTimeStart();
              calculate();
            })
            .setType(new boolean[]{true, true, true, false, false, false})
            .build();
    pvDateEnd = new TimePickerBuilder(this.getContext(),
            (date, v) -> {
              choosedTimeEnd = date;
              updateTimeEnd();
              calculate();
            })
            .setType(new boolean[]{true, true, true, false, false, false})
            .build();

    btn_start_date = view.findViewById(R.id.btn_start_date);
    btn_end_date = view.findViewById(R.id.btn_end_date);

    text_result_date = view.findViewById(R.id.text_result_date);

    btn_start_date.setOnClickListener(v -> pvDateStart.show());
    btn_end_date.setOnClickListener(v -> pvDateEnd.show());

    updateTimeStart();
    updateTimeEnd();
  }
  private void initStrings() {
    text_all = resources.getString(R.string.text_all);
    text_month = resources.getString(R.string.text_month);
    text_day = resources.getString(R.string.text_day);
    text_year = resources.getString(R.string.text_year);
    text_same_day = resources.getString(R.string.text_same_day);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  private Date choosedTimeStart = new Date();
  private Date choosedTimeEnd = new Date();

  private void calculate() {

    Date choosedTimeStartS = new Date();
    Date choosedTimeEndS = new Date();

    if (choosedTimeStart.compareTo(choosedTimeEnd) == 0) {
      text_result_date.setText(text_same_day);
      return;
    }
    if (choosedTimeStart.after(choosedTimeEnd)) {
      choosedTimeStartS = choosedTimeEnd;
      choosedTimeEndS = choosedTimeStart;
    }else{
      choosedTimeStartS = choosedTimeStart;
      choosedTimeEndS = choosedTimeEnd;
    }

    Calendar calendar1 = Calendar.getInstance();
    calendar1.setTime(choosedTimeStartS);
    Calendar calendar2 = Calendar.getInstance();
    calendar2.setTime(choosedTimeEndS);


    int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
    int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
    int year1 = calendar1.get(Calendar.YEAR);
    int year2 = calendar2.get(Calendar.YEAR);
    int month1 = calendar1.get(Calendar.MONTH);
    int month2 = calendar2.get(Calendar.MONTH);

    int daysAll = 0;
    int daysOverlap = 0;
    int months = 0;
    int monthsOverlap = 0;

    //
    //2020/12/07  2021/01/07
    //

    int years = year2 - year1;
    if(year1 <= year2) {
      //计算相差年数
      for (int i = year1; i <= year2; i++) { //闰年
        if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
          daysAll += 366;
        } else { // 不是闰年
          daysAll += 365;
        }
        months += 12;
      }

      years -= 2;

      //同年
      if(year1 == year2) {
        int month1Days = DateUtils.getDayOfMonth(month1, year1);
        monthsOverlap = month2 - month1 - 1;
        daysOverlap += month1Days - day1;
        daysOverlap += day2;
        daysAll = daysOverlap;
        if(daysOverlap > month1Days) {
          monthsOverlap++;
          daysOverlap -= month1Days;
        }
      } else {

        //减去第一年的月天数
        for (int i = 1; i <= month1; i++) {
          if (i == month1) {
            daysAll -= day1;
            daysOverlap += DateUtils.getDayOfMonth(i, year1) - day1;
          } else
            daysAll -= DateUtils.getDayOfMonth(i, year1);
          months--;
        }
        //加上最后一年的月天数
        for (int i = 1; i <= month2; i++) {
          if (i == month2) {
            daysAll += day2 + 1;
            daysOverlap += day2;
          } else {
            daysAll -= DateUtils.getDayOfMonth(i, year2);
            months++;
          }
        }
        //减去最后一年的月数和天数
        for (int i = month2; i <= 12; i++) {
          if (i == month2) daysAll -= DateUtils.getDayOfMonth(i, year2) - day2;
          else daysAll -= DateUtils.getDayOfMonth(i, year2);
          months--;
        }

        monthsOverlap = months % 12;
      }
    }

    if (daysAll <= 0) {
      System.out.println("同一天");
      return;
    }

    StringBuilder result = new StringBuilder();
    if (years > 0) {
      result.append(years);
      result.append(" ");
      result.append(text_year);
      result.append(", ");
    }
    if (monthsOverlap > 0) {
      result.append(monthsOverlap);
      result.append(" ");
      result.append(text_month);
      result.append(", ");
    }
    if (daysOverlap > 0) {
      result.append(daysOverlap);
      result.append(" ");
      result.append(text_day);
    }

    result.append("\n");
    result.append(text_all);
    result.append(" ");
    result.append(daysAll);
    result.append(" ");
    result.append(text_day);

    text_result_date.setText(result);
  }
}
