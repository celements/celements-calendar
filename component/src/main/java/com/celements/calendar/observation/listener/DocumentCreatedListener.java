package com.celements.calendar.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.observation.event.CalendarCreatedEvent;
import com.celements.calendar.observation.event.EventCreatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docCreated")
public class DocumentCreatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentCreatedListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatedEvent());
  }

  public String getName() {
    return "calendar.docCreated";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfCalendar(document, CalendarCreatedEvent.class);
      notifyIfEvent(document, EventCreatedEvent.class);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
