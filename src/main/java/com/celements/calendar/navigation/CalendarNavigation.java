package com.celements.calendar.navigation;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalendarNavigation {

  private final int countBefore;
  private final int countAfter;
  private final int countTotal;

  private final NavigationDetails startNavDetails;
  private final NavigationDetails endNavDetails;
  private final NavigationDetails prevNavDetails;
  private final NavigationDetails nextNavDetails;

  CalendarNavigation(int countBefore, int countAfter, int countTotal,
      NavigationDetails startNavDetails, NavigationDetails endNavDetails,
      NavigationDetails prevNavDetails, NavigationDetails nextNavDetails) {
    this.countBefore = countBefore >= 0 ? countBefore : 0;
    this.countAfter = countAfter >= 0 ? countAfter : 0;
    this.countTotal = countTotal >= 0 ? countTotal : 0;
    this.startNavDetails = startNavDetails;
    this.endNavDetails = endNavDetails;
    this.prevNavDetails = prevNavDetails;
    this.nextNavDetails = nextNavDetails;
  }

  public int getCountBefore() {
    return countBefore;
  }

  public int getCountAfter() {
    return countAfter;
  }

  public int getCountTotal() {
    return countTotal;
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
    return "CalendarNavigation [countBefore=" + countBefore + ", countAfter=" + countAfter
        + ", countTotal=" + countTotal + ", startNavDetails=" + startNavDetails
        + ", endNavDetails=" + endNavDetails + ", prevNavDetails=" + prevNavDetails
        + ", nextNavDetails=" + nextNavDetails + "]";
  }

}
