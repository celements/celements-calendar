package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class CalendarEventSearchQuery extends DefaultEventSearchQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CalendarEventSearchQuery.class);

  private final Date startDate;
  private final boolean isArchive;
  private final List<String> allowedSpaces;
  
  public CalendarEventSearchQuery(ICalendar cal, List<String> sortFields) {
    super(cal.getWikiRef(), getDefaultSortFields(sortFields, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  public CalendarEventSearchQuery(ICalendar cal, IEventSearchQuery query) {
    super(query, getDefaultSortFields(null, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    query.add(getSearchService().createRestrictionGroup(Type.OR, Arrays.asList("space"), 
        getAllowedSpaces()));
    if (!isArchive) {
      query.add(getSearchService().createFromDateRestriction(
          ICalendarClassConfig.CALENDAR_EVENT_CLASS + "." 
          + ICalendarClassConfig.PROPERTY_EVENT_DATE, startDate, true));
    } else {
      query.add(getSearchService().createToDateRestriction(
          ICalendarClassConfig.CALENDAR_EVENT_CLASS + "." 
          + ICalendarClassConfig.PROPERTY_EVENT_DATE, startDate, false));
    }
    return query;
  }

  private List<String> getAllowedSpaces() {
    List<String> ret = new ArrayList<String>();
    if (allowedSpaces != null && allowedSpaces.size() > 0) {
      for(String space : allowedSpaces) {
        ret.add("\"" + space + "\"");
      }
    } else {
      // inexistent space
      ret.add("\".\"");
    }
    return ret;
  }

  @Override
  public String toString() {
    return "CalendarEventSearchQuery [" + super.toString() + ", startDate=" + startDate 
        + ", isArchive=" + isArchive + ", allowedSpaces=" + allowedSpaces + "]";
  }

  static List<String> getDefaultSortFields(List<String> sortFields, boolean inverted) {
    if (sortFields == null || sortFields.isEmpty()) {
      LOGGER.info("getDefaultSortFields: got empty sortFields, using default");
      String pref = (inverted ? "-" : "") + ICalendarClassConfig.CALENDAR_EVENT_CLASS + ".";
      return Arrays.asList(pref + ICalendarClassConfig.PROPERTY_EVENT_DATE,
          pref + ICalendarClassConfig.PROPERTY_EVENT_DATE_END,
          pref + ICalendarClassConfig.PROPERTY_TITLE);
    } else {
      return sortFields;
    }
  }

}
