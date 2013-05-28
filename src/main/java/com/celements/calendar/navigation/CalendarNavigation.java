package com.celements.calendar.navigation;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalendarNavigation {

  private final int countTotal;
  private final int countBefore;
  private final int countAfter;

  private final NavigationDetails startNavDetails;
  private final NavigationDetails endNavDetails;
  private final NavigationDetails prevNavDetails;
  private final NavigationDetails nextNavDetails;

  CalendarNavigation(int countTotal, int countBefore, int countAfter,
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
  public int hashCode() {
    return new HashCodeBuilder().append(countTotal).append(countBefore).append(countAfter
        ).append(startNavDetails).append(endNavDetails).append(prevNavDetails).append(
            nextNavDetails).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CalendarNavigation) {
      CalendarNavigation other = (CalendarNavigation) obj;
      return new EqualsBuilder().append(countTotal, other.countTotal).append(countBefore,
          other.countBefore).append(countAfter, other.countAfter).append(startNavDetails,
              other.startNavDetails).append(endNavDetails, other.endNavDetails).append(
                  prevNavDetails, other.prevNavDetails).append(nextNavDetails,
                      other.nextNavDetails).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "CalendarNavigation [countTotal=" + countTotal + ", countBefore=" + countBefore
        + ", countAfter=" + countAfter + ", startNavDetails=" + startNavDetails
        + ", endNavDetails=" + endNavDetails + ", prevNavDetails=" + prevNavDetails
        + ", nextNavDetails=" + nextNavDetails + "]";
  }

}
