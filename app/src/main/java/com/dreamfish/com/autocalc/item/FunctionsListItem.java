package com.dreamfish.com.autocalc.item;

public class FunctionsListItem {
  public String title;
  public String explain;
  public boolean isHeader;

  public FunctionsListItem(String title, String explain) {
    this.title = title;
    this.explain = explain;
  }
  public FunctionsListItem(String title) {
    this.title = title;
    this.isHeader = true;
  }
}
