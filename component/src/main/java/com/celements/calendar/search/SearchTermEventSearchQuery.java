package com.celements.calendar.search;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.query.LuceneQueryApi;

public class SearchTermEventSearchQuery extends DateEventSearchQuery {
  
  private final String searchTerm;
  private boolean fuzzy;

  public SearchTermEventSearchQuery(String database, Date fromDate, Date toDate, 
      String searchTerm, boolean fuzzy, List<String> sortFields, boolean skipChecks) {
    super(database, fromDate, toDate, sortFields, skipChecks);
    this.searchTerm = searchTerm;
    this.fuzzy = fuzzy;
  }

  public String getSearchTerm() {
    return searchTerm;
  }
  
  public boolean isFuzzy() {
    return fuzzy;
  }

  public LuceneQueryApi getAsLuceneQueryInternal(LuceneQueryApi query) {
    query = super.getAsLuceneQueryInternal(query);
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
    return "SearchTermEventSearchQuery [" + super.toString() + ", searchTerm=" 
        + searchTerm + ", fuzzy=" + fuzzy + "]";
  }

}
