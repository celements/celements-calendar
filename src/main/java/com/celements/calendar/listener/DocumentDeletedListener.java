package com.celements.calendar.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarDeletedEvent;
import com.celements.calendar.event.EventDeletedEvent;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docDeleted")
public class DocumentDeletedListener extends AbstractDocumentListener
  implements EventListener {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      DocumentDeletedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    mLogger.info("getEvents: registering for update, save and delete events.");
    return Arrays.<Event> asList(new DocumentDeletedEvent());
  }

  public String getName() {
    return "calendar.docDeleted";
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
      CalendarDeletedEvent newEvent = new CalendarDeletedEvent();
      getObservationManager().notify(newEvent, source, getCalDataMap(document));
    }
    if (document.getXObject(eventClass) != null) {
      // Fire the user created event
      EventDeletedEvent newEvent = new EventDeletedEvent();
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
