package com.dreamfish.com.autocalc.fragment.datecalc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.utils.DateUtils;
import com.dreamfish.com.autocalc.utils.TextUtils;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DateAddSubCalcFragment extends Fragment {

  public static Fragment newInstance(){
    return new DateAddSubCalcFragment();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_page_date_addsub_calc, null);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initControls(view);
  }

  private TimePickerView pvDateStart;
  private Button btn_start_date;

  private TextView text_result_date;
  private RadioButton radio_add;
  private RadioButton radio_sub;

  private void updateTimeStart() {
    btn_start_date.setText(DateUtils.format(choosedTimeStart, DateUtils.FORMAT_SHORT_CN));
  }
  private void initControls(View view) {

    pvDateStart = new TimePickerBuilder(this.getContext(),
            (date, v) -> {
              choosedTimeStart = date;
              updateTimeStart();
              calculate();
            })
            .setType(new boolean[]{true, true, true, false, false, false})
            .build();

    btn_start_date = view.findViewById(R.id.btn_start_date);
    btn_start_date.setOnClickListener(v -> pvDateStart.show());
    text_result_date = view.findViewById(R.id.text_result_date);

    radio_sub = view.findViewById(R.id.radio_sub);
    radio_add = view.findViewById(R.id.radio_add);

    EditText edit_day = view.findViewById(R.id.edit_day);
    EditText edit_month = view.findViewById(R.id.edit_month);
    EditText edit_year = view.findViewById(R.id.edit_year);

    edit_day.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!TextUtils.isEmpty(s) && TextUtils.isNumber(s)) inputedDay = Integer.parseInt(s.toString());
        else inputedDay = 0;
      }
      @Override
      public void afterTextChanged(Editable s) {
        calculate();
      }
    });
    edit_month.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!TextUtils.isEmpty(s) && TextUtils.isNumber(s)) inputedMonth = Integer.parseInt(s.toString());
        else inputedMonth = 0;
      }
      @Override
      public void afterTextChanged(Editable s) {
        calculate();
      }
    });
    edit_year.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!TextUtils.isEmpty(s) && TextUtils.isNumber(s)) inputedYear = Integer.parseInt(s.toString());
        else inputedYear = 0;
      }
      @Override
      public void afterTextChanged(Editable s) {
        calculate();
      }
    });

    radio_add.setOnClickListener((View v) -> {
      inputIsAdd = true;
      radio_sub.setChecked(false);
      calculate();
    });
    radio_sub.setOnClickListener((View v) -> {
      inputIsAdd = false;
      radio_add.setChecked(false);
      calculate();
    });

    view.findViewById(R.id.btn_clear).setOnClickListener((v) -> {
      edit_year.setText("");
      edit_month.setText("");
      edit_day.setText("");
      calculate();
    });

    updateTimeStart();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  private Date choosedTimeStart = new Date();
  private int inputedDay = 0;
  private int inputedMonth = 0;
  private int inputedYear = 0;
  private boolean inputIsAdd = true;

  private void calculate() {

    Calendar calendar1 = Calendar.getInstance();
    calendar1.setTime(choosedTimeStart);
    calendar1.add(Calendar.DAY_OF_YEAR, inputIsAdd ? inputedDay : -inputedDay);
    calendar1.add(Calendar.MONTH, inputIsAdd ? inputedMonth : -inputedMonth);
    calendar1.add(Calendar.YEAR, inputIsAdd ? inputedYear : -inputedYear);

    text_result_date.setText(DateUtils.format(calendar1.getTime(), DateUtils.FORMAT_SHORT_CN));
  }
}
