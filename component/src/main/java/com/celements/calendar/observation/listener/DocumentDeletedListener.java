package com.celements.calendar.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.observation.event.CalendarDeletedEvent;
import com.celements.calendar.observation.event.EventDeletedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docDeleted")
public class DocumentDeletedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentDeletedListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentDeletedEvent());
  }

  public String getName() {
    return "calendar.docDeleted";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = getOrginialDocument(source);
    if (document != null && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfCalendar(document, CalendarDeletedEvent.class);
      notifyIfEvent(document, EventDeletedEvent.class);
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }
  
  private XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
