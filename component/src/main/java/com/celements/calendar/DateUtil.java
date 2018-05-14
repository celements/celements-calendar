package com.celements.calendar;

import static com.google.common.base.MoreObjects.*;

import java.util.Date;

import org.joda.time.DateTime;

public class DateUtil {

  private static final Date MIN_DATE = new Date(Long.MIN_VALUE);
  private static final Date MAX_DATE = new Date(Long.MAX_VALUE);

  public static boolean between(Date date, Date from, Date to) {
    return intersect(date, date, from, to);
  }

  public static boolean intersect(Date from1, Date to1, Date from2, Date to2) {
    from1 = firstNonNull(from1, MIN_DATE);
    to1 = firstNonNull(to1, MAX_DATE);
    from2 = firstNonNull(from2, MIN_DATE);
    to2 = firstNonNull(to2, MAX_DATE);
    return to1.after(from2) && from1.before(to2);
  }

  public static Date max(Date date, Date defaultDate) {
    return firstNonNull(date, MIN_DATE).after(defaultDate) ? date : defaultDate;
  }

  public static Date min(Date date, Date defaultDate) {
    return firstNonNull(date, MAX_DATE).before(defaultDate) ? date : defaultDate;
  }

  public static int getDayOfWeek(Date date) {
    return new DateTime(date).getDayOfWeek();
  }

  public static boolean isAm(Date date) {
    return new DateTime(date).getHourOfDay() < 12;
  }

  public static Date getDate(int y, int m, int d) {
    return getDate(y, m, d, 0);
  }

  public static Date getDate(int y, int m, int d, int h) {
    return new DateTime(y, m, d, h, 0, 0, 0).toDate();
  }

  public static Date noTime(Date date) {
    if (date != null) {
      return new DateTime(date).withTime(0, 0, 0, 0).toDate();
    }
    return null;
  }

  public static Date endOfDay(Date date) {
    if (date != null) {
      return new DateTime(noTime(date)).plusDays(1).minusMillis(1).toDate();
    }
    return null;
  }

  public static Date getLastHalfDay(Date date) {
    if (date != null) {
      int hour = isAm(date) ? 0 : 12;
      return new DateTime(date).withTime(hour, 0, 0, 0).toDate();
    }
    return null;
  }

  public static Date getNextHalfDay(Date date) {
    if (date != null) {
      DateTime dt = new DateTime(date);
      if (isAm(date)) {
        dt = dt.withTime(12, 0, 0, 0);
      } else {
        dt = dt.plusDays(1).withTime(0, 0, 0, 0);
      }
      return dt.toDate();
    }
    return null;
  }

  public static Date endOfHalfDay(Date date) {
    if (date != null) {
      if (isAm(date)) {
        return new DateTime(date).withTime(11, 59, 59, 999).toDate();
      } else {
        return new DateTime(date).withTime(23, 59, 59, 999).toDate();
      }
    }
    return null;
  }

}
