package com.celements.calendar.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarUpdatedEvent;
import com.celements.calendar.event.EventUpdatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docUpdated")
public class DocumentUpdatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentUpdatedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for update, save and delete events.");
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
      String wikiName = document.getDocumentReference().getWikiReference().getName();
      DocumentReference calClass = getCalendarClasses().getCalendarClassRef(wikiName);
      DocumentReference eventClass = getCalendarClasses().getCalendarEventClassRef(
          wikiName);
      if (document.getXObject(calClass) != null) {
        // Fire the user created event
        CalendarUpdatedEvent newEvent = new CalendarUpdatedEvent();
        getObservationManager().notify(newEvent, source, getCalDataMap(document));
      } else {
        LOGGER.trace("onEvent: no calendar class object found. skipping for calendar. ["
            + document.getDocumentReference() + "].");
      }
      if (document.getXObject(eventClass) != null) {
        // Fire the user created event
        EventUpdatedEvent newEvent = new EventUpdatedEvent();
        getObservationManager().notify(newEvent, source, getEventDataMap(document));
      } else {
        LOGGER.trace("onEvent: no event class object found. skipping for events. ["
            + document.getDocumentReference() + "].");
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  @Override
  protected ComponentManager getComponentManager() {
    return componentManager;
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
