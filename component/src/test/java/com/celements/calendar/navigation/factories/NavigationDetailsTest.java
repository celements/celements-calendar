package com.celements.calendar.navigation.factories;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class NavigationDetailsTest {

  @Test
  public void testNavigationDetails_Date_int() throws Exception {
    Date startDate = new Date();
    NavigationDetails navDetail = NavigationDetails.create(startDate, 20);
    assertEquals(20, navDetail.getOffset());
    assertEquals(startDate, navDetail.getStartDate());
    assertFalse(startDate == navDetail.getStartDate());
  }

  @Test
  public void testEqualsObject_sameDate() throws Exception {
    Date startDate = new Date();
    NavigationDetails navDetail1 = NavigationDetails.create(startDate, 20);
    NavigationDetails navDetail2 = NavigationDetails.create(startDate, 20);
    assertTrue(navDetail1.equals(navDetail2));
    assertTrue(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() == navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_equalDate() throws Exception {
    Calendar cal = java.util.Calendar.getInstance();
    Date startDate1 = cal.getTime();
    Date startDate2 = cal.getTime();
    assertNotSame(startDate1, startDate2);
    NavigationDetails navDetail1 = NavigationDetails.create(startDate1, 20);
    NavigationDetails navDetail2 = NavigationDetails.create(startDate2, 20);
    assertTrue(navDetail1.equals(navDetail2));
    assertTrue(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() == navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_unequalDate() throws Exception {
    Calendar cal = java.util.Calendar.getInstance();
    Date startDate1 = cal.getTime();
    cal.add(Calendar.DATE, 1);
    Date startDate2 = cal.getTime();
    assertNotSame(startDate1, startDate2);
    NavigationDetails navDetail1 = NavigationDetails.create(startDate1, 20);
    NavigationDetails navDetail2 = NavigationDetails.create(startDate2, 20);
    assertFalse(navDetail1.equals(navDetail2));
    assertFalse(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() != navDetail2.hashCode());
  }

  @Test
  public void testEqualsObject_unequalOffset() throws Exception {
    Date startDate = new Date();
    NavigationDetails navDetail1 = NavigationDetails.create(startDate, 25);
    NavigationDetails navDetail2 = NavigationDetails.create(startDate, 20);
    assertFalse(navDetail1.equals(navDetail2));
    assertFalse(navDetail2.equals(navDetail1));
    assertTrue(navDetail1.hashCode() != navDetail2.hashCode());
  }

  @Test
  public void testToString() throws Exception {
    Date date = new Date();
    NavigationDetails navDetail = NavigationDetails.create(date, 5);
    assertEquals("NavigationDetails [startDate=" + date.toString() + ", offset=5]",
        navDetail.toString());
  }

  @Test
  public void testHashCode() throws Exception {
    Calendar cal = java.util.Calendar.getInstance();
    cal.setTimeInMillis(1322575785000L);
    Date startDate = cal.getTime();
    NavigationDetails navDetail = NavigationDetails.create(new Date(0), 0);
    assertEquals(23273, navDetail.hashCode());
    navDetail = NavigationDetails.create(startDate, 0);
    assertEquals(-1553293360, navDetail.hashCode());
    navDetail = NavigationDetails.create(new Date(0), 20);
    assertEquals(23293, navDetail.hashCode());
    navDetail = NavigationDetails.create(startDate, 20);
    assertEquals(-1553293340, navDetail.hashCode());
  }

}
