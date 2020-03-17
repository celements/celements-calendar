package com.celements.calendar.search;

import javax.validation.constraints.NotNull;

import com.celements.calendar.ICalendar;

public interface ICalendarSearchQueryBuilder extends IEventSearchQuery {

  /**
   * must extend the query with restrictions limiting the search to the calendar given. This
   * includes startDate, isArchive and allowedSpaces.
   *
   * @param cal
   */
  @NotNull
  ICalendarSearchQueryBuilder addCalendarRestrictions(@NotNull ICalendar cal);

}
