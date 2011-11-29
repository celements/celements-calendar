package com.celements.calendar.manager;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class NavigationDetailsTest {

  private NavigationDetails navDetail;

  @Before
  public void setUp() throws Exception {
    navDetail = new NavigationDetails();
  }

  @Test
  public void testGetOffset_default() {
    assertEquals("expecting default of -1.", -1, navDetail.getOffset());
  }

  @Test
  public void testSetOffset() {
    navDetail.setOffset(10);
    assertEquals(10, navDetail.getOffset());
  }

  @Test
  public void testGetStartDate_default() {
    assertNull("expecting default null", navDetail.getStartDate());
  }

  @Test
  public void testSetStartDate() {
    Date startDate = new Date();
    navDetail.setStartDate(startDate);
    assertSame("expecting startDate.", startDate, navDetail.getStartDate());
  }

  @Test
  public void testNavigationDetails_Date_int() {
    Date startDate = new Date();
    navDetail = new NavigationDetails(startDate, 20);
    assertEquals(20, navDetail.getOffset());
    assertSame(startDate, navDetail.getStartDate());
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
    assertEquals("[ date : null, offset : -1 ]", navDetail.toString());
  }

  @Test
  public void testHashCode() {
    Calendar cal = java.util.Calendar.getInstance();
    cal.setTimeInMillis(1322575785000L);
    Date startDate = cal.getTime();
    assertEquals(23236, navDetail.hashCode());
    navDetail.setOffset(20);
    assertEquals(24013, navDetail.hashCode());
    navDetail.setStartDate(startDate);
    assertEquals(-274117912, navDetail.hashCode());
  }

}
