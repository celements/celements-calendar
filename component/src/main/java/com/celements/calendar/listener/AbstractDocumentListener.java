package com.celements.calendar.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public abstract class AbstractDocumentListener implements EventListener {

  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calendarClasses;
  
  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  /**
   * The observation manager that will be use to fire user creation events.
   * Note: We can't have the OM as a requirement, since it would create an
   * infinite initialization loop, causing a stack overflow error (this event
   * listener would require an initialized OM and the OM requires a list of
   * initialized event listeners)
   */
  private ObservationManager observationManager;

  protected Map<String, Map<String, String>> getEventDataMap(XWikiDocument document) {
    String wikiName = document.getDocumentReference().getWikiReference().getName();
    DocumentReference eventClass = getCalendarClasses().getCalendarEventClassRef(
        wikiName);
    Map<String, Map<String, String>> multiLangEventData = new HashMap<String,
      Map<String,String>>();
    for (BaseObject eventObj : document.getXObjects(eventClass)) {
      Map<String, String> eventMap = new HashMap<String, String>();
      eventMap.put("lang", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_LANG));
      eventMap.put("l_title", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_TITLE));
      eventMap.put("l_title_rte", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_TITLE_RTE));
      eventMap.put("l_description", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_DESCRIPTION));
      eventMap.put("location", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_LOCATION));
      eventMap.put("location_rte", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_LOCATION_RTE));
      eventMap.put("eventDate", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_EVENT_DATE));
      eventMap.put("eventDate_end", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_EVENT_DATE_END));
      eventMap.put("isSubscribable", document.getXObject(eventClass).getStringValue(
          CalendarClasses.PROPERTY_EVENT_IS_SUBSCRIBABLE));
      multiLangEventData.put(eventObj.getStringValue("lang"), eventMap);
    }
    return multiLangEventData;
  }

  protected Map<String, String> getCalDataMap(XWikiDocument document) {
    String wikiName = document.getDocumentReference().getWikiReference().getName();
    DocumentReference calClass = getCalendarClasses().getCalendarClassRef(wikiName);
    Map<String, String> calData = new HashMap<String, String>();
    calData.put("is_subscribable", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_IS_SUBSCRIBABLE));
    calData.put("event_per_page", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_EVENT_PER_PAGE));
    calData.put("overview_column_config", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_OVERVIEW_COLUMN_CONFIG));
    calData.put("event_column_config", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG));
    calData.put("hasMoreLink", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_HAS_MORE_LINK));
    calData.put("subscribe_to", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_SUBSCRIBE_TO));
    calData.put("calendarspace", document.getXObject(calClass).getStringValue(
        CalendarClasses.PROPERTY_CALENDAR_SPACE));
    return calData;
  }
  
  protected CalendarClasses getCalendarClasses() {
    return ((CalendarClasses) calendarClasses);
  }

  protected ObservationManager getObservationManager() {
    if (this.observationManager == null) {
      try {
        this.observationManager = getComponentManager().lookup(ObservationManager.class);  
      } catch (ComponentLookupException e) {
        throw new RuntimeException(
            "Cound not retrieve an Observation Manager against the component manager");
      }
    }
    return this.observationManager;
  }

  abstract protected ComponentManager getComponentManager();
  
  abstract protected Log getLogger();

}
