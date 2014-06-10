package com.celements.calendar.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarUpdatedEvent;
import com.celements.calendar.event.EventUpdatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docUpdated")
public class DocumentUpdatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentUpdatedListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentUpdatedEvent());
  }

  public String getName() {
    return "calendar.docUpdated";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfCalendar(document, CalendarUpdatedEvent.class);
      notifyIfEvent(document, EventUpdatedEvent.class);
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
