package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.List;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.web.Utils;

public class EventSearchQuery {
  
  protected IQueryService queryService;
  
  protected String spaceName;
  protected String searchTerm;

  public EventSearchQuery(String spaceName, String searchTerm) {
    this.spaceName = spaceName;
    this.searchTerm = searchTerm;
  }

  public LuceneQueryApi getAsLuceneQuery() {
    LuceneQueryApi query = getQueryService().createQuery();
    addRestriction(query, "space", spaceName);
    addOrRestrictions(query, getSearchTermFields(), searchTerm, false, true);
    return query;
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
      query.addRestriction(getRestriction(field, value, tokenize, fuzzy));
    }
  }
  
  protected final void addOrRestrictions(LuceneQueryApi query, String[] fields, 
      String value, boolean tokenize, boolean fuzzy) {
    if (checkString(value)) {
      List<LuceneQueryRestrictionApi> orRestrictionList = 
          new ArrayList<LuceneQueryRestrictionApi>();
      for (String field : fields) {
        orRestrictionList.add(getRestriction(field, value, tokenize, fuzzy));
      }
      query.addOrRestrictionList(orRestrictionList);
    }
  }
  
  protected final LuceneQueryRestrictionApi getRestriction(String field, String value, 
      boolean tokenize, boolean fuzzy) {
    LuceneQueryRestrictionApi restriction = getQueryService().createRestriction(field, 
        value.trim(), tokenize);
    return fuzzy ? restriction.setFuzzy() : restriction;
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
        + "]";
  }
  
  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

}
