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
import org.xwiki.observation.event.Event;

import com.celements.calendar.event.CalendarDeletedEvent;
import com.celements.calendar.event.EventDeletedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("calendar.docDeleted")
public class DocumentDeletedListener extends AbstractDocumentListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocumentDeletedListener.class);

  @Requirement
  private ComponentManager componentManager;

  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for update, save and delete events.");
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
      String wikiName = document.getDocumentReference().getWikiReference().getName();
      DocumentReference calClass = getCalendarClasses().getCalendarClassRef(wikiName);
      DocumentReference eventClass = getCalendarClasses().getCalendarEventClassRef(
          wikiName);  
      if (document.getXObject(calClass) != null) {
        CalendarDeletedEvent newEvent = new CalendarDeletedEvent();
        getObservationManager().notify(newEvent, source, getCalDataMap(document));
      } else {
        LOGGER.trace("onEvent: no calendar class object found. skipping for calendar. ["
            + document.getDocumentReference() + "].");
      }
      if (document.getXObject(eventClass) != null) {
        EventDeletedEvent newEvent = new EventDeletedEvent();
        getObservationManager().notify(newEvent, source, getEventDataMap(document));
      } else {
        LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
            + source + "] and data [" + data + "], isLocalEvent ["
            + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
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
    return LOGGER;
  }

}
