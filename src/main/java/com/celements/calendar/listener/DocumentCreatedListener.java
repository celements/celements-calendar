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
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarCreatedEvent;
import com.celements.calendar.event.EventCreatedEvent;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docCreated")
public class DocumentCreatedListener extends AbstractDocumentListener
  implements EventListener {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      DocumentCreatedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    mLogger.info("getEvents: registering for update, save and delete events.");
    return Arrays.<Event> asList(new DocumentCreatedEvent());
  }

  public String getName() {
    return "calendar.docCreated";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    String wikiName = document.getDocumentReference().getWikiReference().getName();
    DocumentReference calClass = new DocumentReference(wikiName,
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE,
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
    DocumentReference eventClass = new DocumentReference(wikiName,
        CelementsCalendarPlugin.CLASS_EVENT_SPACE,
        CelementsCalendarPlugin.CLASS_EVENT_DOC);

    if (document.getXObject(calClass) != null) {
      // Fire the user created event
      CalendarCreatedEvent newEvent = new CalendarCreatedEvent();
      getObservationManager().notify(newEvent, source, getCalDataMap(document));
    }
    if (document.getXObject(eventClass) != null) {
      // Fire the user created event
      EventCreatedEvent newEvent = new EventCreatedEvent();
      getObservationManager().notify(newEvent, source, getEventDataMap(document));
    }
  }

  @Override
  protected ComponentManager getComponentManager() {
    return componentManager;
  }

  @Override
  protected Log getLogger() {
    return mLogger;
  }

}
