package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.performance.BenchmarkRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.web.Utils;

public class EventSearchResult {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventSearchResult.class);

  private ILuceneSearchService searchService;

  private final LuceneQuery query;
  private final List<String> sortFields;
  private final boolean skipChecks;

  private LuceneSearchResult searchResult;

  private BenchmarkRole benchService;

  EventSearchResult(LuceneQuery query, List<String> sortFields, boolean skipChecks) {
    this.query = query;
    this.sortFields = sortFields;
    this.skipChecks = skipChecks;
  }

  public LuceneSearchResult getSearchResult() {
    if (searchResult == null) {
      if (skipChecks) {
        searchResult = getSearchService().searchWithoutChecks(query, sortFields, null);
        getBenchService().bench("getSearchResult after searchWithoutChecks");
      } else {
        searchResult = getSearchService().search(query, sortFields, null);
        getBenchService().bench("getSearchResult after search");
      }
    }
    return searchResult;
  }

  /**
   * @return all events
   * @throws LuceneSearchException
   */
  public List<IEvent> getEventList() throws LuceneSearchException {
    return getEventList(0, 0);
  }

  /**
   * @param offset
   *          from 0 to (size - 1)
   * @param limit
   *          all remaining events are returned for values < 0 or >= (size - 1)
   * @return selected events
   * @throws LuceneSearchException
   */
  public List<IEvent> getEventList(int offset, int limit) throws LuceneSearchException {
    List<IEvent> eventList = new ArrayList<IEvent>();
    List<EntityReference> results = getSearchResult().getResults(offset, limit);
    getBenchService().bench("getEventList after getResults size: " + results.size());
    for (EntityReference ref : results) {
      if (ref instanceof DocumentReference) {
        eventList.add(new Event((DocumentReference) ref));
      } else {
        LOGGER.warn("getEventList: not expecting Attachment as search result '{}' "
            + "for search '{}'", ref, this);
      }
    }
    getBenchService().bench("getEventList after transform to Event objects");
    return eventList;
  }

  public int getSize() throws LuceneSearchException {
    return getSearchResult().getSize();
  }

  @Override
  public String toString() {
    return "EventSearchResult [searchResult=" + getSearchResult() + ", skipChecks="
        + skipChecks + "]";
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

  BenchmarkRole getBenchService() {
    if (benchService == null) {
      benchService = Utils.getComponent(BenchmarkRole.class);
    }
    return benchService;
  }

}
