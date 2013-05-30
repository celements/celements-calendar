package com.celements.calendar.navigation;

public class UncertainCount {

  private final int count;
  private final boolean uncertain;

  public UncertainCount(int count, boolean uncertain) {
    this.count = count >= 0 ? count : 0;
    this.uncertain = uncertain;
  }

  public int getCount() {
    return count;
  }

  public boolean isUncertain() {
    return uncertain;
  }

}
