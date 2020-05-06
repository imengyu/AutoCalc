package com.dreamfish.com.autocalc;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.dreamfish.com.autocalc.fragment.datecalc.DateAddSubCalcFragment;
import com.dreamfish.com.autocalc.fragment.datecalc.DateDifferCalcFragment;
import com.dreamfish.com.autocalc.utils.MyFragmentAdapter;
import com.dreamfish.com.autocalc.utils.StatusBarUtils;
import com.dreamfish.com.autocalc.widgets.MyTitleBar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class DateRangeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_date_range);

    StatusBarUtils.setLightMode(this);

    MyTitleBar title_bar = findViewById(R.id.title_bar);
    title_bar.setTitle(getTitle());
    title_bar.setLeftIconOnClickListener((View v) -> finish());

    initView();
  }

  private void initView() {
    ViewPager mViewPager = findViewById(R.id.view_pager_main);
    TabLayout mTabLayout = findViewById(R.id.tab_main);
    mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
      }
      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
      }
      @Override
      public void onTabReselected(TabLayout.Tab tab) {
      }
    });
    mTabLayout.setupWithViewPager(mViewPager);

    List<String> sTitle = new ArrayList<>();
    sTitle.add(this.getString(R.string.text_date_diff));
    sTitle.add(this.getString(R.string.text_addsub_date));

    List<Fragment> fragments = new ArrayList<>();
    fragments.add(DateDifferCalcFragment.newInstance());
    fragments.add(DateAddSubCalcFragment.newInstance());

    MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), fragments, sTitle);
    mViewPager.setAdapter(adapter);
    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }
      @Override
      public void onPageSelected(int position) {
      }
      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
