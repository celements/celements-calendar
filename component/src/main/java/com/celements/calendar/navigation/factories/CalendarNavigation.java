package com.celements.calendar.navigation.factories;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalendarNavigation {

  private final UncertainCount countBefore;
  private final UncertainCount countAfter;
  private final UncertainCount countTotal;

  private final NavigationDetails currNavDetails;
  private final NavigationDetails startNavDetails;
  private final NavigationDetails endNavDetails;
  private final NavigationDetails prevNavDetails;
  private final NavigationDetails nextNavDetails;

  CalendarNavigation(UncertainCount countBefore,UncertainCount countAfter,
      UncertainCount countTotal, NavigationDetails currNavDetails,
      NavigationDetails startNavDetails, NavigationDetails endNavDetails,
      NavigationDetails prevNavDetails, NavigationDetails nextNavDetails) {
    this.countBefore = countBefore;
    this.countAfter = countAfter;
    this.countTotal = countTotal;
    this.currNavDetails = currNavDetails;
    this.startNavDetails = startNavDetails;
    this.endNavDetails = endNavDetails;
    this.prevNavDetails = prevNavDetails;
    this.nextNavDetails = nextNavDetails;
  }

  public UncertainCount getCountBefore() {
    return countBefore;
  }

  public UncertainCount getCountAfter() {
    return countAfter;
  }

  public UncertainCount getCountTotal() {
    return countTotal;
  }

  public NavigationDetails getCurrNavDetails() {
    return currNavDetails;
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
    return new HashCodeBuilder().append(countTotal).append(countBefore
        ).append(countAfter).append(currNavDetails).append(startNavDetails).append(
            endNavDetails).append(prevNavDetails).append(nextNavDetails).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CalendarNavigation) {
      CalendarNavigation other = (CalendarNavigation) obj;
      return new EqualsBuilder().append(countTotal, other.countTotal).append(countBefore,
          other.countBefore).append(countAfter, other.countAfter).append(currNavDetails,
              other.currNavDetails).append(startNavDetails, other.startNavDetails).append(
                  endNavDetails, other.endNavDetails).append(prevNavDetails,
                      other.prevNavDetails).append(nextNavDetails, other.nextNavDetails
                          ).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "CalendarNavigation [countBefore=" + countBefore + ", countAfter=" + countAfter
        + ", countTotal=" + countTotal + ", currNavDetails=" + currNavDetails
        + ", startNavDetails=" + startNavDetails + ", endNavDetails=" + endNavDetails
        + ", prevNavDetails=" + prevNavDetails + ", nextNavDetails=" + nextNavDetails
        + "]";
  }

}