package com.celements.calendar.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarCreatedEvent;
import com.celements.calendar.event.EventCreatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docCreated")
public class DocumentCreatedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentCreatedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for update, save and delete events.");
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
      String wikiName = document.getDocumentReference().getWikiReference().getName();
      DocumentReference calClass = getCalendarClasses().getCalendarClassRef(wikiName);
      DocumentReference eventClass = getCalendarClasses().getCalendarEventClassRef(
          wikiName);
      if (document.getXObject(calClass) != null) {
        // Fire the user created event
        CalendarCreatedEvent newEvent = new CalendarCreatedEvent();
        getObservationManager().notify(newEvent, source, getCalDataMap(document));
      } else {
        LOGGER.trace("onEvent: no calendar class object found. skipping for calendar. ["
            + document.getDocumentReference() + "].");
      }
      if (document.getXObject(eventClass) != null) {
        // Fire the user created event
        EventCreatedEvent newEvent = new EventCreatedEvent();
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
