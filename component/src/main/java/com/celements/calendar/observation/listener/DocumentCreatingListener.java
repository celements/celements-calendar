package com.celements.calendar.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.observation.event.EventCreatingEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docCreating")
public class DocumentCreatingListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentCreatingListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatingEvent());
  }

  public String getName() {
    return "calendar.docCreating";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfEvent(document, EventCreatingEvent.class);
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
