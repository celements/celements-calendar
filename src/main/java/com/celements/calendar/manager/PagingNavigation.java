package com.celements.calendar.manager;

public class PagingNavigation {

  private NavigationDetails startNavDetails;
  private NavigationDetails endNavDetails;
  private NavigationDetails prevNavDetails;
  private NavigationDetails nextNavDetails;

  PagingNavigation(NavigationDetails startNavDetails, NavigationDetails endNavDetails,
      NavigationDetails prevNavDetails, NavigationDetails nextNavDetails) {
    this.startNavDetails = startNavDetails;
    this.endNavDetails = endNavDetails;
    this.prevNavDetails = prevNavDetails;
    this.nextNavDetails = nextNavDetails;
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
    return "PagingNavigation [startNavDetails=" + startNavDetails + ", endNavDetails="
        + endNavDetails + ", prevNavDetails=" + prevNavDetails + ", nextNavDetails="
        + nextNavDetails + "]";
  }

}
