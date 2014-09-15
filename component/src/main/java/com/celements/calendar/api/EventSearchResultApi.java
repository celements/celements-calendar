package com.celements.calendar.api;

import java.util.List;

import com.celements.calendar.search.EventSearchResult;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

public class EventSearchResultApi extends Api {

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
    return EventConverter.getEventApiList(searchResult.getEventList(), language, context);
  }

  public List<EventApi> getEventList(int offset, int limit) {
    return EventConverter.getEventApiList(searchResult.getEventList(offset, limit),
        language, context);
  }

  public int getSize() {
    return searchResult.getSize();
  }

  @Override
  public String toString() {
    return searchResult.toString();
  }

}
