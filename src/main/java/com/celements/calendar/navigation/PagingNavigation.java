package com.celements.calendar.navigation;

public class PagingNavigation {

  private int countTotal;
  private int countBefore;
  private int countAfter;

  private NavigationDetails startNavDetails;
  private NavigationDetails endNavDetails;
  private NavigationDetails prevNavDetails;
  private NavigationDetails nextNavDetails;

  PagingNavigation(int countTotal, int countBefore, int countAfter,
      NavigationDetails startNavDetails, NavigationDetails endNavDetails,
      NavigationDetails prevNavDetails, NavigationDetails nextNavDetails) {
    this.countTotal = countTotal;
    this.countBefore = countBefore;
    this.countAfter = countAfter;
    this.startNavDetails = startNavDetails;
    this.endNavDetails = endNavDetails;
    this.prevNavDetails = prevNavDetails;
    this.nextNavDetails = nextNavDetails;
  }

  public int getCountTotal() {
    return countTotal;
  }

  public int getCountBefore() {
    return countBefore;
  }

  public int getCountAfter() {
    return countAfter;
  }

  public NavigationDetails getStartNavDetails() {
    return startNavDetails;
  }

  public NavigationDetails getEndNavDetails() {
    return endNavDetails;
  }

  public NavigationDetails getPrevNavDetails() {
    return prevNavDetails;
  }

  public NavigationDetails getNextNavDetails() {
    return nextNavDetails;
  }

  @Override
  public String toString() {
    return "PagingNavigation [countTotal=" + countTotal + ", countBefore=" + countBefore
        + ", countAfter=" + countAfter + ", startNavDetails=" + startNavDetails
        + ", endNavDetails=" + endNavDetails + ", prevNavDetails=" + prevNavDetails
        + ", nextNavDetails=" + nextNavDetails + "]";
  }

}
