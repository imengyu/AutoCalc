package com.dreamfish.com.autocalc;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dreamfish.com.autocalc.dialog.CommonDialogs;
import com.dreamfish.com.autocalc.fragment.ConverterFragment;
import com.dreamfish.com.autocalc.fragment.MainFragment;
import com.dreamfish.com.autocalc.utils.AlertDialogTool;
import com.dreamfish.com.autocalc.utils.MyFragmentAdapter;
import com.dreamfish.com.autocalc.utils.PermissionsUtils;
import com.dreamfish.com.autocalc.utils.StatusBarUtils;
import com.dreamfish.com.autocalc.utils.UpdaterUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import static com.dreamfish.com.autocalc.dialog.CommonDialogs.RESULT_SETTING_ACTIVITY;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StatusBarUtils.setLightMode(this);
        new UpdaterUtils(this);

        initResources();
        initControl();
        initView();
        initMainMenu();
    }

    private ViewPager mViewPager;
    private MainFragment fragmentMain;
    private ConverterFragment fragmentConverter;

    private Button btn_main_choose;
    private Button btn_convert_choose;
    private Button btn_settings;

    private String currentCalculatorMode = "";
    private String currentConverterMode = "";
    private int currentTabPos = 0;

    private void initView() {
        mViewPager = findViewById(R.id.view_pager_main);

        List<String> sTitle = new ArrayList<>();
        sTitle.add("");
        sTitle.add("");

        fragmentMain = MainFragment.newInstance();
        fragmentConverter = ConverterFragment.newInstance();

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(fragmentMain);
        fragments.add(fragmentConverter);

        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), fragments, sTitle);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                currentTabPos = position;
                updateTabText();
                updateMainMenuState();
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fragmentMain.setOnPadModeChangedListener((String m) -> {
            currentCalculatorMode = m;
            updateTabText();
        });
        fragmentConverter.setOnModeChangedListener((String m) -> {
            currentConverterMode = m;
            updateTabText();
        });

    }
    private void initControl() {
        btn_main_choose = findViewById(R.id.btn_main_choose);
        btn_convert_choose = findViewById(R.id.btn_convert_choose);
        btn_settings = findViewById(R.id.btn_settings);
        btn_main_choose.setOnClickListener((View v) -> {
            if(mViewPager.getCurrentItem() == 0) fragmentMain.chooseMode();
            else mViewPager.setCurrentItem(0);
        });
        btn_convert_choose.setOnClickListener((View v) -> {
            if(mViewPager.getCurrentItem() == 1) fragmentConverter.showChooseConvertDialog();
            else mViewPager.setCurrentItem(1);
        });
        btn_settings.setOnClickListener((v) -> mainMenu.show());
    }

    private void updateTabText() {
        switch (currentTabPos) {
            case 0:
                btn_main_choose.setTextColor(tab_text_color_selected);
                btn_main_choose.setText(MessageFormat.format(text_calculator_mode, currentCalculatorMode));
                btn_main_choose.setCompoundDrawables(null,null, ic_down_small_primary, null);
                btn_convert_choose.setCompoundDrawables(null,null,null, null);
                btn_convert_choose.setText(text_converter);
                btn_convert_choose.setTextColor(tab_text);
                break;
            case 1:
                btn_main_choose.setTextColor(tab_text);
                btn_main_choose.setText(text_calculator);
                btn_main_choose.setCompoundDrawables(null,null, null, null);
                btn_convert_choose.setCompoundDrawables(null,null,ic_down_small_primary, null);
                btn_convert_choose.setText(currentConverterMode);
                btn_convert_choose.setTextColor(tab_text_color_selected);
                break;
        }
    }

    private int tab_text;
    private int tab_text_color_selected;

    private String text_calculator_mode;
    private String text_calculator;
    private String text_converter;
    private Drawable ic_down_small_primary;

    private void initResources() {
        Resources resources = getResources();
        tab_text = resources.getColor(R.color.tab_text, null);
        tab_text_color_selected = resources.getColor(R.color.tab_text_color_selected, null);
        text_calculator_mode = resources.getString(R.string.text_calculator_mode);
        text_calculator = resources.getString(R.string.text_calculator);
        text_converter = resources.getString(R.string.text_converter);
        ic_down_small_primary = resources.getDrawable(R.drawable.ic_down_small_primary, null);
        ic_down_small_primary.setBounds(1, 1, 30, 30);
    }

    private PopupMenu mainMenu;

    private void updateMainMenuState() {
        Menu menu = mainMenu.getMenu();
        switch (currentTabPos) {
            case 0:
                menu.findItem(R.id.action_show_functions).setEnabled(true);
                menu.findItem(R.id.action_custom_input).setEnabled(true);
                menu.findItem(R.id.action_show_calc_step).setEnabled(true);
                menu.findItem(R.id.action_show_full).setEnabled(true);
                break;
            case 1:
                menu.findItem(R.id.action_show_functions).setEnabled(false);
                menu.findItem(R.id.action_custom_input).setEnabled(false);
                menu.findItem(R.id.action_show_calc_step).setEnabled(false);
                menu.findItem(R.id.action_show_full).setEnabled(false);
                break;
        }
    }
    private void initMainMenu() {
        mainMenu = new PopupMenu(MainActivity.this, btn_settings);
        mainMenu.getMenuInflater().inflate(R.menu.menu_main, mainMenu.getMenu());
        mainMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_exit:
                    finish();
                    break;
                case R.id.action_help:
                    CommonDialogs.showHelp(this);
                    break;
                case R.id.action_settings:
                    CommonDialogs.showSettings(this);
                    break;
                case R.id.action_show_full:
                    fragmentMain.showFullText();
                    break;
                case R.id.action_show_functions:
                    fragmentMain.showAllFunctionsHelp();
                    break;
                case R.id.action_custom_input:
                    fragmentMain.showCustomerView();
                    break;
                case R.id.action_show_calc_step:
                    fragmentMain.showCalcStep();
                    break;
            }
            return true;
        });
    }

    //退出提示
    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //首先判断用户有没有按下返回键
        if (keyCode== KeyEvent.KEYCODE_BACK){
            //判断用户按下的时间是不是大于2秒，如果大于2秒则认为是失误操作
            if ((System.currentTimeMillis()-mExitTime)>2000){
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime=System.currentTimeMillis();//记住当前时间，下次再按返回键时做对比
            }else {
                finish();
            }
            return true;//返回true不在往下运行
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SETTING_ACTIVITY) {
            fragmentMain.updateSettings();
            fragmentConverter.updateSettings();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


}
