package com.celements.calendar.search;

import java.util.Date;

public interface IDateEventSearchQuery extends IEventSearchQuery {

  Date getToDate();

  Date getFromDate();

}
