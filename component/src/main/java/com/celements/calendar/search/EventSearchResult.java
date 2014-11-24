package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.web.Utils;

public class EventSearchResult {
  
  private ILuceneSearchService searchService;
  
  private final LuceneQuery query;
  private final List<String> sortFields;
  
  private LuceneSearchResult searchResult;

  EventSearchResult(LuceneQuery query, List<String> sortFields) {
    this.query = query;
    this.sortFields = sortFields;
  }

  public LuceneSearchResult getSearchResult() {
    if (searchResult == null) {
      if (getSearchService().skipChecks()) {
        searchResult = getSearchService().searchWithoutChecks(query, sortFields, null);
      } else {
        searchResult = getSearchService().search(query, sortFields, null);
      }
    }
    return searchResult;
  }

  /**
   * 
   * @return all events
   * @throws LuceneSearchException 
   */
  public List<IEvent> getEventList() throws LuceneSearchException {
    return getEventList(0, 0);
  }

  /**
   * 
   * @param offset from 0 to (size - 1)
   * @param limit all remaining events are returned for values < 0 or >= (size - 1)
   * @return selected events
   * @throws LuceneSearchException 
   */
  public List<IEvent> getEventList(int offset, int limit) throws LuceneSearchException {
    List<IEvent> eventList = new ArrayList<IEvent>();
    for (DocumentReference docRef : getSearchResult().getResults(offset, limit)) {
      eventList.add(new Event(docRef));
    }
    return Collections.unmodifiableList(eventList);
  }

  public int getSize() throws LuceneSearchException {
    return getSearchResult().getSize();
  }

  @Override
  public String toString() {
    return "EventSearchResult [searchResult=" + getSearchResult() + "]";
  }

  private ILuceneSearchService getSearchService() {
    if (searchService == null) {
      searchService = Utils.getComponent(ILuceneSearchService.class);
    }
    return searchService;
  }

  void injectSearchService(ILuceneSearchService searchService) {
    this.searchService = searchService;
  }

}
