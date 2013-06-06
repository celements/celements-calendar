package com.celements.calendar.navigation.factories;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.navigation.factories.UncertainCount;

public class CalendarNavigationTest {

  @Test
  public void test_getters() {
    long time = new Date().getTime();
    int offset = 20;
    int count = 5;
    UncertainCount countBefore = new UncertainCount(count++, false);
    UncertainCount countAfter = new UncertainCount(count++, false);
    UncertainCount countTotal = new UncertainCount(count++, false);
    NavigationDetails startNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails endNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails prevNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails nextNavDetails = new NavigationDetails(new Date(time++), offset++);
    CalendarNavigation calNav =  new CalendarNavigation(countBefore, countAfter, countTotal,
        startNavDetails, endNavDetails, prevNavDetails, nextNavDetails);

    assertSame(countBefore, calNav.getCountBefore());
    assertSame(countAfter, calNav.getCountAfter());
    assertSame(countTotal, calNav.getCountTotal());
    assertSame(startNavDetails, calNav.getStartNavDetails());
    assertSame(endNavDetails, calNav.getEndNavDetails());
    assertSame(prevNavDetails, calNav.getPrevNavDetails());
    assertSame(nextNavDetails, calNav.getNextNavDetails());
  }

  private CalendarNavigation getCalNav(long time, int offset, int count) {
    UncertainCount countBefore = new UncertainCount(count++, false);
    UncertainCount countAfter = new UncertainCount(count++, false);
    UncertainCount countTotal = new UncertainCount(count++, false);
    NavigationDetails startNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails endNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails prevNavDetails = new NavigationDetails(new Date(time++), offset++);
    NavigationDetails nextNavDetails = new NavigationDetails(new Date(time++), offset++);
    return new CalendarNavigation(countBefore, countAfter, countTotal,
        startNavDetails, endNavDetails, prevNavDetails, nextNavDetails);
  }


  @Test
  public void test_equals_hashCode() {
    CalendarNavigation calNav1 =  getCalNav(new Date(1000).getTime(), 20, 5);
    CalendarNavigation calNav2 =  getCalNav(new Date(1000).getTime(), 20, 5);
    assertEquals(calNav1, calNav2);
    assertEquals(calNav1.hashCode(), calNav2.hashCode());
    calNav1 =  getCalNav(new Date(1000).getTime(), 20, 5);
    calNav2 =  getCalNav(new Date(1001).getTime(), 20, 5);
    assertFalse(calNav1.equals(calNav2));
    assertFalse(calNav1.hashCode() == calNav2.hashCode());
    calNav1 =  getCalNav(new Date(1000).getTime(), 20, 5);
    calNav2 =  getCalNav(new Date(1000).getTime(), 30, 5);
    assertFalse(calNav1.equals(calNav2));
    assertFalse(calNav1.hashCode() == calNav2.hashCode());
    calNav1 =  getCalNav(new Date(1000).getTime(), 20, 5);
    calNav2 =  getCalNav(new Date(1000).getTime(), 20, 10);
    assertFalse(calNav1.equals(calNav2));
    assertFalse(calNav1.hashCode() == calNav2.hashCode());
  }

}
