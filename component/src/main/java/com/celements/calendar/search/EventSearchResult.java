package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.web.Utils;

public class EventSearchResult {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(EventSearchResult.class);
  
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
  public List<IEvent> getEventList() {
    return getEventList(0, 0);
  }

  /**
   * 
   * @param offset from 0 to (size - 1)
   * @param limit all remaining events are returned for values < 0 or >= (size - 1)
   * @return selected events
   * @throws LuceneSearchException 
   */
  public List<IEvent> getEventList(int offset, int limit) {
    List<IEvent> eventList = new ArrayList<IEvent>();
    try {
      for (DocumentReference docRef : getSearchResult().getResults(offset, limit)) {
        eventList.add(new Event(docRef));
      }
    } catch (LuceneSearchException lse) {
      // XXX do not catch here
      LOGGER.error("Error while executing lucene query", lse);
    }
    return Collections.unmodifiableList(eventList);
  }

  public int getSize() {
    int ret = 0;
    try {
      ret = getSearchResult().getSize();
    } catch (LuceneSearchException lse) {
      // XXX do not catch here
      LOGGER.error("Error while executing lucene query", lse);
    }
    return ret;
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
