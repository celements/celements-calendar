package com.celements.calendar.search;

import com.celements.calendar.ICalendar;

public interface ICalendarSearchQueryBuilder extends IEventSearchQuery {

  void addCalendarRestrictions(ICalendar cal);

}
