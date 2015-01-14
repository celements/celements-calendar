package com.celements.calendar.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.observation.event.EventDeletingEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docDeleting")
public class DocumentDeletingListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentDeletingListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentDeletingEvent());
  }

  public String getName() {
    return "calendar.docDeleting";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if (document != null && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfEvent(document, EventDeletingEvent.class);
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
