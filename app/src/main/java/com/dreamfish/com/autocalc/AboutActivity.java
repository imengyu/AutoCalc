package com.dreamfish.com.autocalc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.dreamfish.com.autocalc.dialog.CommonDialogs;
import com.dreamfish.com.autocalc.utils.StatusBarUtils;
import com.dreamfish.com.autocalc.widgets.MyTitleBar;

public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    StatusBarUtils.setLightMode(this);

    MyTitleBar title_bar = findViewById(R.id.title_bar);
    title_bar.setTitle(getTitle());
    title_bar.setLeftIconOnClickListener((View v) -> finish());

    findViewById(R.id.btn_ok).setOnClickListener((v) -> {
      finish();
    });
    findViewById(R.id.btn_help).setOnClickListener((v) -> {
      CommonDialogs.showHelp(this);
    });
  }
}
