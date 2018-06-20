package com.celements.calendar;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.calendar.api.EventApi;
import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class MoreFunctions {

  private MoreFunctions() {
  }

  public static Function<List<IEvent>, List<EventApi>> calendarEventListToApiFunction() {
    return CALENDAR_EVENT_LIST_TO_API;
  }

  private static final Function<List<IEvent>, List<EventApi>> CALENDAR_EVENT_LIST_TO_API = new Function<List<IEvent>, List<EventApi>>() {

    @Override
    public List<EventApi> apply(List<IEvent> events) {
      return FluentIterable.from(events).filter(Predicates.notNull()).transform(
          calendarEventToApiFunction()).toList();
    }
  };

  public static Function<IEvent, EventApi> calendarEventToApiFunction() {
    return CALENDAR_EVENT_TO_API;
  }

  private static final Function<IEvent, EventApi> CALENDAR_EVENT_TO_API = new Function<IEvent, EventApi>() {

    private final Logger LOGGER = LoggerFactory.getLogger(MoreFunctions.class);

    @Override
    public EventApi apply(IEvent event) {
      try {
        return EventApi.create(event, getContext());
      } catch (XWikiException xwe) {
        LOGGER.error("Exception vonverting IEvent [{}] to EventApi", event, xwe);
      }
      return null;
    }

    XWikiContext getContext() {
      return Utils.getComponent(ModelContext.class).getXWikiContext();
    }
  };

}
