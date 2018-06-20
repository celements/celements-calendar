package com.celements.calendar.morefunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.celements.model.context.ModelContext;
import com.google.common.base.Function;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class CalendarEventToApiFunction implements Function<IEvent, EventApi> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEventToApiFunction.class);

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
}
