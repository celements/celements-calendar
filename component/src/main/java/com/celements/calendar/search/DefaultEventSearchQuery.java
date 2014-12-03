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

  public DefaultEventSearchQuery(String database) {
    this(database, null, null);
  }

  public DefaultEventSearchQuery(IEventSearchQuery query, List<String> altSortFields) {
    this(query.getDatabase(), query.getAsLuceneQuery(), getSortFields(query, 
        altSortFields));
  }

  public DefaultEventSearchQuery(String database, List<String> sortFields) {
    this(database, null, sortFields);
  }

  public DefaultEventSearchQuery(String database, LuceneQuery luceneQuery, 
      List<String> sortFields) {
    this.database = database;
    this.luceneQuery = luceneQuery;
    if (sortFields != null) {
      this.sortFields = Collections.unmodifiableList(new ArrayList<String>(sortFields));
    } else {
      this.sortFields = Collections.emptyList();
    }
  }

  @Override
  public String getDatabase() {
    return database;
  }

  @Override
  public List<String> getSortFields() {
    return sortFields;
  }

  @Override
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
        + luceneQuery + ", sortFields=" + sortFields + "]";
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
