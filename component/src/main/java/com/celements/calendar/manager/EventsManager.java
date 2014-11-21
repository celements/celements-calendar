package com.celements.calendar.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("default")
public class EventsManager implements IEventManager {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      EventsManager.class);

  @Requirement
  private ICalendarService calService;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calClasses;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Deprecated
  public List<EventApi> getEvents(ICalendar cal, int start, int nb) {
    List<EventApi> eventApiList = new ArrayList<EventApi>();
    for (IEvent event : getEventsInternal(cal, start, nb)) {
      eventApiList.add(new EventApi(event, cal.getLanguage(), getContext()));
    }
    return eventApiList;
  }

  public List<IEvent> getAllEventsInternal(ICalendar cal) {
    return getEventsInternal(cal, 0, 0);
  }

  public List<IEvent> getEventsInternal(ICalendar cal, int start, int nb) {
    long time = System.currentTimeMillis();
    List<IEvent> eventList = Collections.emptyList();
    DocumentReference calDocRef = cal.getDocumentReference();
    try {
      String lang = webUtilsService.getDefaultLanguage();
      List<String> allowedSpaces = calService.getAllowedSpaces(calDocRef);
      if (nb == 0) {
        //FIXME eventList==null causes NPE in filterEventListForSubscription
        eventList = cal.getEngine().getEvents(cal.getStartDate(), cal.isArchive(), lang,
            allowedSpaces);
      } else {
        eventList = cal.getEngine().getEvents(cal.getStartDate(), cal.isArchive(), lang,
            allowedSpaces, start, nb);
      }
      filterEventListForSubscription(cal.getDocumentReference(), eventList);
    } catch (XWikiException exc) {
      LOGGER.error("Error while getting events from calendar '" + calDocRef + "'", exc);
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("getEventsInternal: " + eventList.size() + " events found for cal '" 
        + calDocRef + "', start '" + start + "', nb '" + nb + "' and took " + time + "ms");
    return eventList;
  }

  private List<IEvent> filterEventListForSubscription(DocumentReference calDocRef,
      List<IEvent> eventList) throws XWikiException {
    Iterator<IEvent> iter = eventList.iterator();
    while (iter.hasNext()) {
      IEvent event = iter.next();
      if (!checkEventSubscription(calDocRef, event)) {
        iter.remove();
        LOGGER.debug("filterEventListForSubscription: filtered '" + event + "'");
      }
    }
    return eventList;
  }

  private boolean checkEventSubscription(DocumentReference calDocRef, IEvent event
      ) throws XWikiException {
    return isHomeCalendar(calDocRef, event.getDocumentReference())
        || isEventSubscribed(calDocRef, event);
  }

  boolean isHomeCalendar(DocumentReference calDocRef, DocumentReference eventDocRef
      ) throws XWikiException {
    String eventSpaceForCal = calService.getEventSpaceForCalendar(calDocRef);
    boolean isHomeCal = eventDocRef.getLastSpaceReference().getName(
        ).equals(eventSpaceForCal);
    LOGGER.trace("isHomeCalendar: for [" + eventDocRef + "] check on calDocRef ["
        + calDocRef + "] with space [" + eventSpaceForCal + "] returning " + isHomeCal);
    return isHomeCal;
  }

  private boolean isEventSubscribed(DocumentReference calDocRef, IEvent event) {
    BaseObject obj = getSubscriptionObject(calDocRef, event);
    ICalendar calendar = event.getCalendar();
    BaseObject calObj = null;
    if ((calendar != null) && (calendar.getCalDoc() != null)) {
      calObj = calendar.getCalDoc().getXObject(getCalClasses().getCalendarClassRef(
          getContext().getDatabase()));
    }
    boolean isSubscribed = false;
    if ((obj != null) && (obj.getIntValue("doSubscribe") == 1) && (calObj != null)
        && (calObj.getIntValue("is_subscribable") == 1)) {
      isSubscribed = true;
    }
    LOGGER.trace("isEventSubscribed: for [" + event.getDocumentReference()
        + "] returning " + isSubscribed);
    return isSubscribed;
  }

  private BaseObject getSubscriptionObject(DocumentReference calDocRef, IEvent event) {
    XWikiDocument eventDoc = event.getEventDocument();
    BaseObject subscriptObj = eventDoc.getXObject(getCalClasses(
        ).getSubscriptionClassRef(getContext().getDatabase()), "subscriber",
        webUtilsService.getRefDefaultSerializer().serialize(calDocRef), false);
    if (subscriptObj == null) {
      // for backwards compatibility
      subscriptObj = eventDoc.getXObject(getCalClasses().getSubscriptionClassRef(
          getContext().getDatabase()), "subscriber",
          webUtilsService.getRefLocalSerializer().serialize(calDocRef), false);
    }
    return subscriptObj;
  }

  public EventSearchResult searchEvents(ICalendar cal, IEventSearchQuery query) {
    long time = System.currentTimeMillis();
    EventSearchResult eventsResult = null;
    int size = 0;
    DocumentReference calDocRef = cal.getDocumentReference();
    try {
      eventsResult = cal.getEngine().searchEvents(query, cal.getStartDate(), 
          cal.isArchive(), webUtilsService.getDefaultLanguage(), 
          calService.getAllowedSpaces(calDocRef));
      LOGGER.debug("EventsManager searchEvents: isArchive [" + cal.isArchive()
          + "], startDate [" + cal.getStartDate() + "] query [" + query + "].");
      //XXX calling getSize() imediatelly is a dirty Workaround!!!
      //XXX accessing results directly prevents lucene inconsistancies
      //XXX if multiple results are created (e.g. in Navigation).
      size = eventsResult.getSize();
    } catch (XWikiException exc) {
      LOGGER.error("Error while searching events in calendar '" + calDocRef + "'", exc);
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("searchEvents: " + size + " events found for cal '" + calDocRef 
        + "', query '" + query + "' and took " + time + "ms");
    return eventsResult;
  }

  /**
   * 
   * @param calDoc
   * @param isArchive
   * @return
   * 
   * @deprecated instead use countEvents(DocumentReference, boolean)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive) {
    return countEvents(calDoc, isArchive, new Date());
  }

  /**
   * 
   * @param calDoc
   * @param isArchive
   * @param startDate
   * @return
   * 
   * @deprecated instead use countEvents(DocumentReference, boolean, Date)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate) {
    return countEvents(calDoc.getDocumentReference(), isArchive, startDate);
  }

  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive) {
    return countEvents(calDocRef, isArchive, new Date());
  }

  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive, Date startDate) {
    ICalendar cal = calService.getCalendarByCalRef(calDocRef, isArchive);
    cal.setStartDate(startDate);
    return countEvents(cal);
  }

  public long countEvents(ICalendar cal) {
    long time = System.currentTimeMillis();
    long count = 0;
    DocumentReference calDocRef = cal.getDocumentReference();
    try {
      String cacheKey = "EventsManager.countEvents|" 
          + webUtilsService.getRefDefaultSerializer().serialize(calDocRef) + "|"
          + cal.isArchive() + "|" + cal.getStartDate().getTime();
      Object cachedCount = execution.getContext().getProperty(cacheKey);
      if (cachedCount != null) {
        LOGGER.debug("Cached event count: " + cachedCount);
        count = (Long) cachedCount;
      } else {
        count = countEvents_internal(cal);
        if (count > 0) {
          execution.getContext().setProperty(cacheKey, count);
        }
      }
    } catch (XWikiException exc) {
      LOGGER.error("Exception while counting events for calendar '" + calDocRef + "'",
          exc);
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("countEvents: got " + count + " for cal '" + calDocRef + "' and took " 
        + time + "ms");
    return count;
  }

  private long countEvents_internal(ICalendar cal) throws XWikiException {
    DocumentReference calDocRef = cal.getDocumentReference();
    boolean isArchive = cal.isArchive();
    Date startDate = cal.getStartDate();
    long count = 0;
    if ((calDocRef != null) && getContext().getWiki().exists(calDocRef, getContext())) {
      count = cal.getEngine().countEvents(startDate, isArchive,
          webUtilsService.getDefaultLanguage(), calService.getAllowedSpaces(calDocRef));
      LOGGER.debug("Event count for calendar '" + calDocRef + "' with startDate + '"
          + startDate + "' and isArchive '" + isArchive + "': " + count);
    }
    return count;
  }

  public IEvent getEvent(DocumentReference eventDocRef) {
    return new Event(eventDocRef);
  }

  public IEvent getFirstEvent(ICalendar cal) {
    long time = System.currentTimeMillis();
    IEvent event = null;
    DocumentReference calDocRef = cal.getDocumentReference();
    try {
      event = cal.getEngine().getFirstEvent(cal.getStartDate(), cal.isArchive(),
          webUtilsService.getDefaultLanguage(), calService.getAllowedSpaces(calDocRef));
    } catch (XWikiException exc) {
      LOGGER.error("Exception while getting first event date for calendar '" + calDocRef
          + "'", exc);
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("getFirstEvent: got " + event + " for cal '" + calDocRef + "' and took " 
        + time + "ms");
    return event;
  }

  public IEvent getLastEvent(ICalendar cal) {
    long time = System.currentTimeMillis();
    IEvent event = null;
    DocumentReference calDocRef = cal.getDocumentReference();
    try {
      event = cal.getEngine().getLastEvent(cal.getStartDate(), cal.isArchive(),
          webUtilsService.getDefaultLanguage(), calService.getAllowedSpaces(calDocRef));
    } catch (XWikiException exc) {
      LOGGER.error("Exception while getting last event date for calendar '" + calDocRef
          + "'", exc);
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("getLastEvent: got " + event + " for cal '" + calDocRef + "' and took " 
        + time + "ms");
    return event;
  }

  private CalendarClasses getCalClasses() {
    return (CalendarClasses) calClasses;
  }

  void injectExecution(Execution execution) {
    this.execution = execution;
  }

  void injectCalService(ICalendarService calService) {
    this.calService = calService;
  }

}
