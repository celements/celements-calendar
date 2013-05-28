package com.celements.calendar.navigation;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class NavigationDetailsTest {

  @Test
  public void testNavigationDetails_Date_int() {
    Date startDate = new Date();
    NavigationDetails navDetail = new NavigationDetails(startDate, 20);
    assertEquals(20, navDetail.getOffset());
    assertEquals(startDate, navDetail.getStartDate());
    assertFalse(startDate == navDetail.getStartDate());
  }

  @Test
  public void testEqualsObject_sameDate() {
    Date startDate = new Date();
    NavigationDetails navDetail1 = new NavigationDetails(startDate, 20);
    NavigationDetails navDetail2 = new NavigationDetails(startDate, 20);
    assertTrue(navDetail1.equals(navDetail2));
    assertTrue(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() == navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_equalDate() {
    Calendar cal = java.util.Calendar.getInstance();
    Date startDate1 = cal.getTime();
    Date startDate2 = cal.getTime();
    assertNotSame(startDate1, startDate2);
    NavigationDetails navDetail1 = new NavigationDetails(startDate1, 20);
    NavigationDetails navDetail2 = new NavigationDetails(startDate2, 20);
    assertTrue(navDetail1.equals(navDetail2));
    assertTrue(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() == navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_unequalDate() {
    Calendar cal = java.util.Calendar.getInstance();
    Date startDate1 = cal.getTime();
    cal.add(Calendar.DATE, 1);
    Date startDate2 = cal.getTime();
    assertNotSame(startDate1, startDate2);
    NavigationDetails navDetail1 = new NavigationDetails(startDate1, 20);
    NavigationDetails navDetail2 = new NavigationDetails(startDate2, 20);
    assertFalse(navDetail1.equals(navDetail2));
    assertFalse(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() != navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_unequalOffset() {
    Date startDate = new Date();
    NavigationDetails navDetail1 = new NavigationDetails(startDate, 25);
    NavigationDetails navDetail2 = new NavigationDetails(startDate, 20);
    assertFalse(navDetail1.equals(navDetail2));
    assertFalse(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() != navDetail2.hashCode());
  }

  @Test
  public void testToString() {
    NavigationDetails navDetail = new NavigationDetails(null, -1);
    assertEquals("NavigationDetails [startDate=null, offset=-1]", navDetail.toString());
  }

  @Test
  public void testHashCode() {
    Calendar cal = java.util.Calendar.getInstance();
    cal.setTimeInMillis(1322575785000L);
    Date startDate = cal.getTime();
    NavigationDetails navDetail = new NavigationDetails(null, -1);
    assertEquals(23236, navDetail.hashCode());
    navDetail = new NavigationDetails(null, 20);
    assertEquals(24013, navDetail.hashCode());
    navDetail = new NavigationDetails(startDate, 20);
    assertEquals(-274117912, navDetail.hashCode());
  }

}
