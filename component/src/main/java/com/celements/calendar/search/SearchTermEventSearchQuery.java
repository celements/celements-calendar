package com.celements.calendar.search;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.query.LuceneQueryApi;

public class SearchTermEventSearchQuery extends DefaultEventSearchQuery {

  private final Date fromDate;
  private final Date toDate;
  private final String searchTerm;
  private boolean fuzzy;

  public SearchTermEventSearchQuery(String database, Date fromDate, Date toDate, 
      String searchTerm, boolean fuzzy) {
    super(database);
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.searchTerm = searchTerm;
    this.fuzzy = fuzzy;
  }

  public SearchTermEventSearchQuery(String database, Date fromDate, Date toDate, 
      String searchTerm, boolean fuzzy, List<String> sortFields, boolean skipChecks) {
    super(database, sortFields, skipChecks);
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.searchTerm = searchTerm;
    this.fuzzy = fuzzy;
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

  public String getSearchTerm() {
    return searchTerm;
  }
  
  public boolean isFuzzy() {
    return fuzzy;
  }

  public LuceneQueryApi getAsLuceneQueryInternal(LuceneQueryApi query) {
    query = super.getAsLuceneQueryInternal(query);
    query.addRestriction(getQueryService().createFromToDateRestriction(
        CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE, 
        fromDate, toDate, true));
    if (StringUtils.isNotBlank(searchTerm)) {
      List<String> fields = getSearchTermFields();
      for (String s : searchTerm.split(",")) {
        query.addOrRestrictionList(getQueryService().createRestrictionList(fields, s, 
            true, fuzzy));
      }
    }
    return query;
  }

  protected List<String> getSearchTermFields() {
    return Arrays.asList(CalendarClasses.CALENDAR_EVENT_CLASS + "." 
        + CalendarClasses.PROPERTY_TITLE, CalendarClasses.CALENDAR_EVENT_CLASS + "." 
            + CalendarClasses.PROPERTY_DESCRIPTION);
  }

  @Override
  public String toString() {
    return "SearchTermEventSearchQuery [" + super.toString() + ", fromDate=" + fromDate 
        + ", toDate=" + toDate + ", searchTerm=" + searchTerm + ", fuzzy=" + fuzzy + "]";
  }

}
