package com.celements.calendar.api;

import java.util.ArrayList;
import java.util.List;

import com.celements.calendar.IEvent;
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
    return searchResult.getLuceneQuery();
  }

  public String[] getSortFields() {
    return searchResult.getSortFields();
  }

  public List<EventApi> getEventList() {
    return getEventApiList(searchResult.getEventList());
  }

  public List<EventApi> getEventList(int offset, int limit) {
    return getEventApiList(searchResult.getEventList(offset, limit));
  }

  public int getSize() {
    return searchResult.getSize();
  }

  @Override
  public String toString() {
    return searchResult.toString();
  }

  private List<EventApi> getEventApiList(List<IEvent> eventList) {
    List<EventApi> eventApiList = new ArrayList<EventApi>();
    for (IEvent event : eventList) {
      eventApiList.add(getEventApi(event));
    }
    return eventApiList;
  }

  private EventApi getEventApi(IEvent event) {
    EventApi eventApi = null;
    if (event != null) {
      eventApi = new EventApi(event, language, context);
    }
    return eventApi;
  }

}
