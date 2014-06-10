package com.celements.calendar.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.EventUpdatingEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docUpdating")
public class DocumentUpdatingListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentUpdatingListener.class);

  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentUpdatingEvent());
  }

  public String getName() {
    return "calendar.docUpdating";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      notifyIfEvent(document, EventUpdatingEvent.class);
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
