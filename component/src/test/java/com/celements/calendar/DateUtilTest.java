package com.celements.calendar;

import static com.celements.calendar.DateUtil.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTimeConstants;
import org.junit.Test;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class DateUtilTest {

  Date start = getDate(8);
  Date end = getDate(14);

  @Test
  public void test_intersect_inside() {
    Date from = getDate(10);
    Date to = getDate(13);
    assertTrue(intersect(from, to, start, end));
    assertTrue(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_leftIntersect() {
    Date from = getDate(6);
    Date to = getDate(9);
    assertTrue(intersect(from, to, start, end));
    assertTrue(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_rightIntersect() {
    Date from = getDate(13);
    Date to = getDate(15);
    assertTrue(intersect(from, to, start, end));
    assertTrue(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_before() {
    Date from = getDate(4);
    Date to = getDate(7);
    assertFalse(intersect(from, to, start, end));
    assertFalse(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_after() {
    Date from = getDate(15);
    Date to = getDate(18);
    assertFalse(intersect(from, to, start, end));
    assertFalse(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_false_leftToch() {
    Date from = getDate(6);
    Date to = getDate(8);
    assertFalse(intersect(from, to, start, end));
    assertFalse(intersect(start, end, from, to));
  }

  @Test
  public void test_intersect_false_rightToch() {
    Date from = getDate(14);
    Date to = getDate(15);
    assertFalse(intersect(from, to, start, end));
    assertFalse(intersect(start, end, from, to));
  }

  @Test
  public void test_max() {
    assertEquals(end, max(start, end));
    assertEquals(end, max(end, start));
    assertEquals(end, max(null, end));
  }

  @Test
  public void test_min() {
    assertEquals(start, min(start, end));
    assertEquals(start, min(end, start));
    assertEquals(start, min(null, start));
  }

  @Test
  public void test_getDayOfWeek() {
    assertEquals(DateTimeConstants.SUNDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 1)));
    assertEquals(DateTimeConstants.MONDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 2)));
    assertEquals(DateTimeConstants.TUESDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 3)));
    assertEquals(DateTimeConstants.WEDNESDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 4)));
    assertEquals(DateTimeConstants.THURSDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 5)));
    assertEquals(DateTimeConstants.FRIDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 6)));
    assertEquals(DateTimeConstants.SATURDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 7)));
    assertEquals(DateTimeConstants.SUNDAY, getDayOfWeek(DateUtil.getDate(2017, 1, 8)));
    for (int hour : createRange(0, 23)) {
      assertEquals(DateTimeConstants.SUNDAY, getDayOfWeek(getDate(hour)));
    }
  }

  @Test
  public void test_isAm() {
    for (int hour : createRange(0, 11)) {
      assertTrue(isAm(getDate(hour)));
    }
    for (int hour : createRange(12, 23)) {
      assertFalse(isAm(getDate(hour)));
    }
  }

  @Test
  public void test_noTime() {
    assertEquals(getDate(0), noTime(getDate(2)));
  }

  @Test
  public void test_getLastHalfDay() {
    assertEquals(getDate(0), getLastHalfDay(getDate(8)));
    assertEquals(getDate(12), getLastHalfDay(getDate(15)));
  }

  @Test
  public void test_getNextHalfDay() {
    assertEquals(getDate(12), getNextHalfDay(getDate(8)));
    assertEquals(DateUtil.getDate(2017, 1, 2), getNextHalfDay(getDate(15)));
  }

  @Test
  public void test_endOfHalfDay_0_specificDate_normalDay() {
    Calendar calExpect = Calendar.getInstance();
    calExpect.clear();
    calExpect.set(2017, Calendar.AUGUST, 21, 11, 59, 59);
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2017, Calendar.AUGUST, 21, 0, 0, 0);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    assertEquals(sdf.format(calExpect.getTime()), sdf.format(endOfHalfDay(cal.getTime())));
  }

  @Test
  // change on 26.3.2017
  public void test_endOfHalfDay_0_specificDate_startSummertime() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    for (int i = 25; i <= 27; i++) {
      Calendar calExpect = Calendar.getInstance();
      calExpect.clear();
      calExpect.set(2017, Calendar.MARCH, i, 11, 59, 59);
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2017, Calendar.MARCH, i, 0, 0, 0);
      assertEquals(sdf.format(calExpect.getTime()), sdf.format(endOfHalfDay(cal.getTime())));
    }
  }

  @Test
  // change on 29.10.2017
  public void test_endOfHalfDay_0_specificDate_startWintertime() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    for (int i = 28; i <= 30; i++) {
      Calendar calExpect = Calendar.getInstance();
      calExpect.clear();
      calExpect.set(2017, Calendar.OCTOBER, i, 11, 59, 59);
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2017, Calendar.OCTOBER, i, 0, 0, 0);
      assertEquals(sdf.format(calExpect.getTime()), sdf.format(endOfHalfDay(cal.getTime())));
    }
  }

  // is a sunday
  private Date getDate(int hours) {
    return DateUtil.getDate(2017, 1, 1, hours);
  }

  private Iterable<Integer> createRange(int lower, int upper) {
    return ContiguousSet.create(Range.closed(lower, upper), DiscreteDomain.integers());
  }

}
