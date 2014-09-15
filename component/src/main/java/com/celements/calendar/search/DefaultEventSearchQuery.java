package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.xpn.xwiki.web.Utils;

public class DefaultEventSearchQuery implements IEventSearchQuery {

  private ILuceneSearchService searchService;
  
  private final String database;
  private final LuceneQuery luceneQuery;
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
  
  public DefaultEventSearchQuery(String database, LuceneQuery luceneQuery, 
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

  public final LuceneQuery getAsLuceneQuery() {
    LuceneQuery query = luceneQuery;
    if (query == null) {
      query = getSearchService().createQuery(database);
    }
    return getAsLuceneQueryInternal(query);
  }
  
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {    
    QueryRestriction eventObjRestr = getSearchService().createObjectRestriction(
        getCalEventClassRef());
    if (!query.contains(eventObjRestr)) {
      query.add(eventObjRestr);
    }
    return query;
  }
  
  protected final DocumentReference getCalEventClassRef() {
    return ((CalendarClasses) Utils.getComponent(IClassCollectionRole.class, 
        "celements.CalendarClasses")).getCalendarEventClassRef(getDatabase());
  }

  protected final ILuceneSearchService getSearchService() {
    if (searchService == null) {
      searchService = Utils.getComponent(ILuceneSearchService.class);
    }
    return searchService;
  }
  
  @Override
  public String toString() {
    return "DefaultEventSearchQuery [database=" + database + ", luceneQuery=" 
        + luceneQuery + ", sortFields=" + sortFields + ", skipChecks=" + skipChecks + "]";
  }

  void injectQueryService(ILuceneSearchService searchService) {
    this.searchService = searchService;
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
