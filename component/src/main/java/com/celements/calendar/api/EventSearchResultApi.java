package com.celements.calendar.api;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.calendar.search.EventSearchResult;
import com.celements.search.lucene.LuceneSearchException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

public class EventSearchResultApi extends Api {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventSearchResultApi.class);

  private final EventSearchResult searchResult;
  private final String language;

  public EventSearchResultApi(EventSearchResult searchResult, XWikiContext context) {
    super(context);
    this.searchResult = searchResult;
    this.language = context.getLanguage();
  }

  public EventSearchResultApi(EventSearchResult searchResult, String language,
      XWikiContext context) {
    super(context);
    this.searchResult = searchResult;
    this.language = language;
  }

  public String getLuceneQuery() {
    return searchResult.getSearchResult().getQueryString();
  }

  public List<String> getSortFields() {
    return searchResult.getSearchResult().getSortFields();
  }

  public List<EventApi> getEventList() {
    try {
      return EventApi.createList(searchResult.getEventList(), language, 
          context);
    } catch (Exception exc) {
      LOGGER.error("Error getting all events", exc);
    }
    return Collections.emptyList();
  }

  public List<EventApi> getEventList(int offset, int limit) {
    try {
      return EventApi.createList(searchResult.getEventList(offset, limit),
          language, context);
    } catch (Exception exc) {
      LOGGER.error("Error getting {} events starting at {}", limit, offset, exc);
    }
    return Collections.emptyList();
  }

  public int getSize() {
    try {
      return searchResult.getSize();
    } catch (LuceneSearchException exc) {
      LOGGER.error("Error getting event size", exc);
    }
    return 0;
  }

  @Override
  public String toString() {
    return searchResult.toString();
  }

}
