package com.dreamfish.com.autocalc.utils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyFragmentAdapter extends FragmentStatePagerAdapter {
  private List<Fragment> mFragments ;
  private List<String> mTitles ;
  public MyFragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
    super(fm);
    mFragments = fragments;
    mTitles = titles;
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    return mFragments.get(position);
  }

  @Override
  public int getCount() {
    return mFragments == null ? 0 : mFragments.size();
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return mTitles.get(position);
  }
}
