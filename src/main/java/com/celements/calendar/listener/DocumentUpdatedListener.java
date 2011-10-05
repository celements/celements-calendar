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
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarUpdatedEvent;
import com.celements.calendar.event.EventUpdatedEvent;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docUpdated")
public class DocumentUpdatedListener extends AbstractDocumentListener
  implements EventListener {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      DocumentUpdatedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    mLogger.info("getEvents: registering for update, save and delete events.");
    return Arrays.<Event> asList(new DocumentUpdatedEvent());
  }

  public String getName() {
    return "calendar.docUpdated";
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
      CalendarUpdatedEvent newEvent = new CalendarUpdatedEvent();
      getObservationManager().notify(newEvent, source, getCalDataMap(document));
    }
    if (document.getXObject(eventClass) != null) {
      // Fire the user created event
      EventUpdatedEvent newEvent = new EventUpdatedEvent();
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
