package com.celements.calendar.navigation.factories;

import java.util.Date;

public class NavigationDetails {

  private final Date startDate;
  private final int offset;

  NavigationDetails(Date startDate, int offset){
    if ((startDate == null) || (offset < 0)) {
      throw new IllegalArgumentException();
    }
    this.offset = offset;
    this.startDate = startDate;
  }

  public Date getStartDate() {
    return new Date(startDate.getTime());
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NavigationDetails)) {
      return false;
    }
    NavigationDetails theObj = (NavigationDetails) obj;
    boolean equalDate = (theObj.startDate == this.startDate)
        || ((theObj.startDate != null) && (this.startDate != null)
            && theObj.startDate.equals(this.startDate));
    return (equalDate && (theObj.offset == this.offset));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = (37*result) + offset;
    result = (37*result) + computeStartDateHash();
    return result;
  }

  private int computeStartDateHash() {
    if (startDate ==  null) {
      return 0;
    } else {
      return startDate.hashCode();
    }
  }

  @Override
  public String toString() {
    return "NavigationDetails [startDate=" + startDate + ", offset=" + offset + "]";
  }

}
