package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.web.Utils;

public class DefaultEventSearchQuery implements IEventSearchQuery {

  private ILuceneSearchService searchService;

  private final WikiReference wikiRef;
  private final LuceneQuery luceneQuery;
  private final List<String> sortFields;

  public DefaultEventSearchQuery(WikiReference wikiRef) {
    this(wikiRef, null, null);
  }

  public DefaultEventSearchQuery(IEventSearchQuery query, List<String> altSortFields) {
    this(query.getWikiRef(), query.getAsLuceneQuery(), getSortFields(query, 
        altSortFields));
  }

  public DefaultEventSearchQuery(WikiReference wikiRef, List<String> sortFields) {
    this(wikiRef, null, sortFields);
  }

  public DefaultEventSearchQuery(WikiReference wikiRef, LuceneQuery luceneQuery, 
      List<String> sortFields) {
    this.wikiRef = wikiRef;
    this.luceneQuery = luceneQuery;
    if (sortFields != null) {
      this.sortFields = Collections.unmodifiableList(new ArrayList<String>(sortFields));
    } else {
      this.sortFields = Collections.emptyList();
    }
  }

  @Deprecated
  @Override
  public String getDatabase() {
    String database = null;
    if (getWikiRef() != null) {
      database = getWikiRef().getName();
    }
    return database;
  }

  @Override
  public WikiReference getWikiRef() {
    return wikiRef;
  }

  @Override
  public List<String> getSortFields() {
    return sortFields;
  }

  @Override
  public final LuceneQuery getAsLuceneQuery() {
    LuceneQuery query = luceneQuery;
    if (query == null) {
      query = getSearchService().createQuery();
    }
    if (wikiRef != null) {
      query.setWiki(wikiRef);
    }
    return getAsLuceneQueryInternal(query);
  }

  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query.add(getSearchService().createObjectRestriction(getCalEventClassRef()));
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
    return "DefaultEventSearchQuery [wikiRef=" + wikiRef + ", luceneQuery=" 
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
