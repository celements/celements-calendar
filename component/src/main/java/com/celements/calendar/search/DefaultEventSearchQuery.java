package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.web.Utils;

public class DefaultEventSearchQuery implements IEventSearchQuery {

  private IQueryService queryService;
  
  private final String database;
  private final LuceneQueryApi luceneQuery;
  private final List<String> sortFields;
  private final boolean skipChecks;
  
  public DefaultEventSearchQuery(String database) {
    this(database, null, null, false);
  }

  public DefaultEventSearchQuery(IEventSearchQuery query, List<String> altSortFields) {
    this(query.getDatabase(), query.getAsLuceneQuery(), getSortFields(query, 
        altSortFields), query.skipChecks());
  }

  public DefaultEventSearchQuery(String database, List<String> sortFields, 
      boolean skipChecks) {
    this(database, null, sortFields, skipChecks);
  }
  
  public DefaultEventSearchQuery(String database, LuceneQueryApi luceneQuery, 
      List<String> sortFields, boolean skipChecks) {
    this.database = database;
    this.luceneQuery = luceneQuery;
    if (sortFields != null) {
      this.sortFields = Collections.unmodifiableList(new ArrayList<String>(sortFields));
    } else {
      this.sortFields = Collections.emptyList();
    }
    this.skipChecks = skipChecks;
  }
  
  public String getDatabase() {
    return database;
  }
  
  public List<String> getSortFields() {
    return sortFields;
  }
  
  public boolean skipChecks() {
    return skipChecks;
  }

  public final LuceneQueryApi getAsLuceneQuery() {
    LuceneQueryApi query = luceneQuery;
    if (query == null) {
      query = getQueryService().createQuery(database);
    }
    return getAsLuceneQueryInternal(query);
  }
  
  protected LuceneQueryApi getAsLuceneQueryInternal(LuceneQueryApi query) {
    LuceneQueryRestrictionApi eventObjRestr = getQueryService().createRestriction(
        "object", "\"" + CalendarClasses.CALENDAR_EVENT_CLASS + "\"");
    if (!query.getQueryString().contains(eventObjRestr.getRestriction())) {
      query.addRestriction(eventObjRestr);
    }
    return query;
  }

  protected final IQueryService getQueryService() {
    if (queryService == null) {
      queryService = Utils.getComponent(IQueryService.class);
    }
    return queryService;
  }
  
  @Override
  public String toString() {
    return "DefaultEventSearchQuery [database=" + database + ", luceneQuery=" 
        + luceneQuery + ", sortFields=" + sortFields + ", skipChecks=" + skipChecks + "]";
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }
  
  private static List<String> getSortFields(IEventSearchQuery query,
      List<String> altSortFields) {
    if (query.getSortFields() == null || query.getSortFields().isEmpty()) {
      return altSortFields;
    } else {
      return query.getSortFields();
    }
  }

}
