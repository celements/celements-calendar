package com.celements.calendar.manager;

import java.util.Date;

public class NavigationDetails {

  private int offset = -1;
  
  private Date startDate;

  NavigationDetails(Date startDate, Integer offset) {
    this.offset = offset;
    this.startDate = startDate;
  }

  public int getOffset() {
    return offset;
  }
  
  void setOffset(int offset) {
    this.offset = offset;
  }
  
  public Date getStartDate() {
    return startDate;
  }

  void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NavigationDetails)) {
      return false;
    }
    NavigationDetails theObj = (NavigationDetails) obj;
    boolean equalDate = (theObj.getStartDate() == this.getStartDate())
        || ((theObj.getStartDate() != null) && (this.getStartDate() != null)
        && theObj.getStartDate().equals(this.getStartDate()));
    return (equalDate && (theObj.getOffset() == this.getOffset()));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37*result + offset;
    result = 37*result + computeStartDateHash();
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
    return "[ date : " + startDate + ", offset : " + offset + "]";
  }
  
}
