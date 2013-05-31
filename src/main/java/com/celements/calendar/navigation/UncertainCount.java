package com.celements.calendar.navigation;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(count).append(uncertain).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UncertainCount) {
      UncertainCount other = (UncertainCount) obj;
      return new EqualsBuilder().append(count, other.count).append(uncertain,
          other.uncertain).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "UncertainCount [count=" + count + ", uncertain=" + uncertain + "]";
  }

}
