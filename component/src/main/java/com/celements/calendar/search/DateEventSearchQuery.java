package com.celements.calendar.search;

import java.util.Date;
import java.util.List;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.query.LuceneQuery;

public class DateEventSearchQuery extends DefaultEventSearchQuery {

  private final Date fromDate;
  private final Date toDate;

  public DateEventSearchQuery(String database, Date fromDate, Date toDate, 
      List<String> sortFields, boolean skipChecks) {
    this(database, null, fromDate, toDate, sortFields, skipChecks);
  }

  public DateEventSearchQuery(String database, LuceneQuery luceneQuery, Date fromDate, 
      Date toDate, List<String> sortFields, boolean skipChecks) {
    super(database, luceneQuery, sortFields, skipChecks);
    this.fromDate = fromDate;
    this.toDate = toDate;
  }
  
  public Date getFromDate() {
    if (fromDate != null) {
      return new Date(fromDate.getTime());
    }
    return null;
  }

  public Date getToDate() {
    if (toDate != null) {
      return new Date(toDate.getTime());
    }
    return null;
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    query.add(getSearchService().createFromToDateRestriction(
        CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE, 
        fromDate, toDate, true));
    return query;
  }

  @Override
  public String toString() {
    return "DateEventSearchQuery [" + super.toString() + ", fromDate=" + fromDate 
        + ", toDate=" + toDate + "]";
  }

}
