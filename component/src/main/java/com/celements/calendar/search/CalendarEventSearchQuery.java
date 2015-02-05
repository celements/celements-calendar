package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.calendar.ICalendar;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class CalendarEventSearchQuery extends DefaultEventSearchQuery {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarEventSearchQuery.class);

  private final Date startDate;
  private final boolean isArchive;
  private final String lang;
  private final List<String> allowedSpaces;
  
  public CalendarEventSearchQuery(ICalendar cal, List<String> sortFields) {
    super(cal.getWikiRef(), getDefaultSortFields(sortFields, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.lang = cal.getLanguage();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  public CalendarEventSearchQuery(ICalendar cal, IEventSearchQuery query) {
    super(query, getDefaultSortFields(null, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.lang = cal.getLanguage();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    query.add(getSearchService().createRestrictionGroup(Type.OR, Arrays.asList("space"), 
        getAllowedSpaces()));
    query.add(getSearchService().createFieldRestriction(getCalEventClassRef(), 
        CalendarClasses.PROPERTY_LANG, "\"" + lang + "\""));
    if (!isArchive) {
      query.add(getSearchService().createFromDateRestriction(
          CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE,
          startDate, true));
    } else {
      query.add(getSearchService().createToDateRestriction(
          CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE,
          startDate, false));
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
        + ", isArchive=" + isArchive + ", lang=" + lang + ", allowedSpaces=" 
        + allowedSpaces + "]";
  }

  static List<String> getDefaultSortFields(List<String> sortFields, boolean inverted) {
    if (sortFields == null || sortFields.isEmpty()) {
      LOGGER.info("getDefaultSortFields: got empty sortFields, using default");
      String pref = (inverted ? "-" : "") + CalendarClasses.CALENDAR_EVENT_CLASS + ".";
      return Arrays.asList(pref + CalendarClasses.PROPERTY_EVENT_DATE,
          pref + CalendarClasses.PROPERTY_EVENT_DATE_END,
          pref + CalendarClasses.PROPERTY_TITLE);
    } else {
      return sortFields;
    }
  }

}
