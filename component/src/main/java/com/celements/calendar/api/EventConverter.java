package com.celements.calendar.api;

import java.util.ArrayList;
import java.util.List;

import com.celements.calendar.IEvent;
import com.xpn.xwiki.XWikiContext;

public class EventConverter {

  static List<EventApi> getEventApiList(List<IEvent> eventList, XWikiContext context) {
    return getEventApiList(eventList, context.getLanguage(), context);
  }

  static List<EventApi> getEventApiList(List<IEvent> eventList, String language,
      XWikiContext context) {
    List<EventApi> eventApiList = new ArrayList<EventApi>();
    for (IEvent event : eventList) {
      eventApiList.add(getEventApi(event, language, context));
    }
    return eventApiList;
  }

  static EventApi getEventApi(IEvent event, XWikiContext context) {
    return getEventApi(event, context.getLanguage(), context);
  }

  static EventApi getEventApi(IEvent event, String language, XWikiContext context) {
    EventApi eventApi = null;
    if (event != null) {
      eventApi = new EventApi(event, language, context);
    }
    return eventApi;
  }

}
