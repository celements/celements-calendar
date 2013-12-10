package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.web.Utils;

public class EventSearchQuery {

  protected IQueryService queryService;

  protected final String spaceName;
  protected final Date fromDate;
  protected final Date toDate;
  protected final String searchTerm;
  protected boolean fuzzy;

  public EventSearchQuery(String spaceName, Date fromDate, Date toDate,
      String searchTerm) {
    this.spaceName = spaceName;
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.searchTerm = searchTerm;
    fuzzy = false;
  }

  public String getSpaceName() {
    return spaceName;
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

  public LuceneQueryApi getAsLuceneQuery() {
    LuceneQueryApi query = getQueryService().createQuery();
    addRestriction(query, "space", "\"" + spaceName + "\"");
    addDateRestriction(query, getEventDateFieldName(), fromDate, toDate);
    if (checkString(searchTerm)) {
      String[] fields = getSearchTermFields();
      for (String s : searchTerm.split(",")) {
        addOrRestrictions(query, fields, s, true, fuzzy);
      }
    }
    return query;
  }

  private String getEventDateFieldName() {
    return CalendarClasses.CALENDAR_EVENT_CLASS + "."
        + CalendarClasses.PROPERTY_EVENT_DATE;
  }

  protected String[] getSearchTermFields() {
    return new String[] {
        CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_TITLE,
        CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_DESCRIPTION
    };
  }

  protected final void addRestriction(LuceneQueryApi query, String field, String value) {
    addRestriction(query, field, value, true, false);
  }

  protected final void addRestriction(LuceneQueryApi query, String field, String value,
      boolean tokenize, boolean fuzzy) {
    if (checkString(value)) {
      query.addRestriction(getQueryService().createRestriction(field, value.trim(), 
          tokenize, fuzzy));
    }
  }

  protected final void addOrRestrictions(LuceneQueryApi query, String[] fields,
      String value, boolean tokenize, boolean fuzzy) {
    if (checkString(value)) {
      List<LuceneQueryRestrictionApi> orRestrictionList =
          new ArrayList<LuceneQueryRestrictionApi>();
      for (String field : fields) {
        orRestrictionList.add(getQueryService().createRestriction(field, value.trim(), 
            tokenize, fuzzy));
      }
      query.addOrRestrictionList(orRestrictionList);
    }
  }

  protected final void addDateRestriction(LuceneQueryApi query, String field,
      Date fromDate, Date toDate) {
    query.addRestriction(getQueryService().createFromToDateRestriction(field, fromDate, 
        toDate, true));
  }

  protected final boolean checkString(String s) {
    return (s != null) && (!s.trim().isEmpty());
  }

  protected final IQueryService getQueryService() {
    if (queryService == null) {
      queryService = Utils.getComponent(IQueryService.class);
    }
    return queryService;
  }

  @Override
  public String toString() {
    return "EventSearchQuery [spaceName=" + spaceName + ", searchTerm=" + searchTerm
        + ", fuzzy=" + fuzzy + "]";
  }

  public void setFuzzy(boolean fuzzy) {
    this.fuzzy = fuzzy;
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

}
