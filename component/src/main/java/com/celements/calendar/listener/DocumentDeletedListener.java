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

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.event.CalendarDeletedEvent;
import com.celements.calendar.event.EventDeletedEvent;
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
    XWikiDocument document = getOrginialDocument(source);
    if (document != null) {
      mLogger.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      String wikiName = document.getDocumentReference().getWikiReference().getName();
      DocumentReference calClass = new DocumentReference(wikiName, 
          CalendarClasses.CALENDAR_CONFIG_CLASS_SPACE,
          CalendarClasses.CALENDAR_CONFIG_CLASS_DOC);
      DocumentReference eventClass = new DocumentReference(wikiName,
          CalendarClasses.CALENDAR_EVENT_CLASS_SPACE,
          CalendarClasses.CALENDAR_EVENT_CLASS_DOC);
  
      if (document.getXObject(calClass) != null) {
        CalendarDeletedEvent newEvent = new CalendarDeletedEvent();
        getObservationManager().notify(newEvent, source, getCalDataMap(document));
      } else {
        mLogger.trace("onEvent: no calendar class object found. skipping for calendar. ["
            + document.getDocumentReference() + "].");
      }
      if (document.getXObject(eventClass) != null) {
        EventDeletedEvent newEvent = new EventDeletedEvent();
        getObservationManager().notify(newEvent, source, getEventDataMap(document));
      } else {
        mLogger.trace("onEvent: no event class object found. skipping for events. ["
            + document.getDocumentReference() + "].");
      }
    }
  }
  
  private XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
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
