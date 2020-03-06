package com.celements.calendar.search;

import java.util.Date;
import java.util.List;

import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.search.lucene.query.LuceneQuery;

public class DateEventSearchQuery extends DefaultEventSearchQuery implements IDateEventSearchQuery {

  private final Date fromDate;
  private final Date toDate;

  public DateEventSearchQuery(WikiReference wikiRef, Date fromDate, Date toDate,
      List<String> sortFields) {
    this(wikiRef, null, fromDate, toDate, sortFields);
  }

  public DateEventSearchQuery(WikiReference wikiRef, LuceneQuery luceneQuery, Date fromDate,
      Date toDate, List<String> sortFields) {
    super(wikiRef, luceneQuery, sortFields);
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  @Override
  public Date getFromDate() {
    if (fromDate != null) {
      return new Date(fromDate.getTime());
    }
    return null;
  }

  @Override
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
        ICalendarClassConfig.CALENDAR_EVENT_CLASS + "." + ICalendarClassConfig.PROPERTY_EVENT_DATE,
        fromDate, toDate, true));
    return query;
  }

  @Override
  public String toString() {
    return "DateEventSearchQuery [" + super.toString() + ", fromDate=" + fromDate + ", toDate="
        + toDate + "]";
  }

}
