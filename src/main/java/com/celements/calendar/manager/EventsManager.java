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
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.service.CalendarService;
import com.celements.calendar.service.ICalendarService;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("default")
public class EventsManager implements IEventManager {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      EventsManager.class);

  //TODO we must change to 'default' serializer with the wikiname included
  @Requirement("local")
  private EntityReferenceSerializer<String> refLocalSerializer;

  @Requirement("default")
  private EntityReferenceSerializer<String> refDefaultSerializer;

  @Requirement
  private ICalendarService calService;

  @Requirement
  private IQueryService queryService;

  @Requirement
  private IEventSearch eventSearch;

  @Requirement
  private IWebUtilsService webUtilsService;

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
    try {
      return getEvents_internal(cal.getDocumentReference(), start, nb, cal.isArchive(),
          cal.getStartDate());
    } catch (XWikiException exc) {
      LOGGER.error(exc);
    }
    return Collections.emptyList();
  }

  private List<IEvent> getEvents_internal(DocumentReference calDocRef, int start, int nb,
      boolean isArchive, Date startDate) throws XWikiException {
    List<IEvent> eventList = getEventSearchResult(calDocRef, isArchive, startDate
        ).getEventList(start, nb);
    LOGGER.debug(eventList.size() + " events found.");
    return filterEventListForSubscription(calDocRef, eventList);
  }

  private List<IEvent> filterEventListForSubscription(DocumentReference calDocRef,
      List<IEvent> eventList) throws XWikiException {
    Iterator<IEvent> iter = eventList.iterator();
    while (iter.hasNext()) {
      IEvent event = iter.next();
      if(!checkEventSubscription(calDocRef, event)){
        iter.remove();
        LOGGER.debug("filterEventListForSubscription: filtered '" + event + "'");
      }
    }
    return eventList;
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

  public long countEvents(ICalendar cal) {
    return countEvents(cal.getDocumentReference(), cal.isArchive(), cal.getStartDate());
  }

  public long countEvents(DocumentReference calDocRef, boolean isArchive) {
    return countEvents(calDocRef, isArchive, new Date());
  }

  public long countEvents(DocumentReference calDocRef, boolean isArchive, Date startDate) {
    String cacheKey = "EventsManager.countEvents|" + refDefaultSerializer.serialize(
        calDocRef) + "|" + isArchive + "|" + startDate.getTime();
    Object cachedCount = execution.getContext().getProperty(cacheKey);
    if (cachedCount != null) {
      LOGGER.debug("Cached event count: " + cachedCount);
      return (Long) cachedCount;
    } else {
      try {
        long count = getEventSearchResult(calDocRef, isArchive, startDate).getSize();
        LOGGER.debug("Event count for calendar '" + calDocRef + "' with startDate + '"
            + startDate + "' and isArchive '" + isArchive + "': " + count);
        execution.getContext().setProperty(cacheKey, count);
        return count;
      } catch (XWikiException exc) {
        LOGGER.error("Exception while counting events for calendar '" + calDocRef
            + "' with startDate '" + startDate + "' and isArchive '" + isArchive + "'",
            exc);
      }
    }
    return 0;
  }

  private EventSearchResult getEventSearchResult(DocumentReference calDocRef,
      boolean isArchive, Date startDate) throws XWikiException {
    LuceneQueryApi query = queryService.createQuery();
    addSpaceRestrictions(query, calDocRef);
    addLangRestriction(query, calDocRef);
    if (!isArchive) {
      return eventSearch.getSearchResultFromDate(query, startDate);
    } else {
      return eventSearch.getSearchResultUptoDate(query, startDate);
    }
  }

  private void addSpaceRestrictions(LuceneQueryApi query, DocumentReference calDocRef
      ) throws XWikiException {
    List<String> allowedSpaces = calService.getAllowedSpaces(calDocRef);
    if (!allowedSpaces.isEmpty()) {
      List<LuceneQueryRestrictionApi> spaceRestrictionList =
          new ArrayList<LuceneQueryRestrictionApi>();
      for (String space : allowedSpaces) {
        spaceRestrictionList.add(queryService.createRestriction("space", space));
      }
      query.addOrRestrictionList(spaceRestrictionList);
    }
  }

  private void addLangRestriction(LuceneQueryApi query, DocumentReference calDocRef) {
    String defaultLang = webUtilsService.getDefaultLanguage();
    LuceneQueryRestrictionApi langRestriction = queryService.createRestriction(
        Event.CLASS + "." + Event.PROPERTY_LANG, defaultLang);
    query.addRestriction(langRestriction);
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
        + calDocRef + "] with space [" + eventSpaceForCal
        + "] returning " + isHomeCal);
    return isHomeCal;
  }

  private boolean isEventSubscribed(DocumentReference calDocRef, IEvent event
      ) throws XWikiException {
    BaseObject obj = getSubscriptionObject(calDocRef, event);
    ICalendar calendar = event.getCalendar();
    BaseObject calObj = null;
    if ((calendar != null) && (calendar.getCalDoc() != null)){
      calObj = calendar.getCalDoc().getXObject(getCalenderConfigClassRef());
    }
    boolean isSubscribed = false;
    if((obj != null) && (obj.getIntValue("doSubscribe") == 1)
        && (calObj != null) && (calObj.getIntValue("is_subscribable") == 1)){
      isSubscribed = true;
    }
    LOGGER.trace("isEventSubscribed: for [" + event.getDocumentReference()
        + "] returning " + isSubscribed);
    return isSubscribed;
  }

  private DocumentReference getCalenderConfigClassRef() {
    return new DocumentReference(getContext().getDatabase(),
        CalendarService.CLASS_CALENDAR_SPACE,
        CalendarService.CLASS_CALENDAR_DOC);
  }

  private BaseObject getSubscriptionObject(DocumentReference calDocRef, IEvent event) {
    XWikiDocument eventDoc = event.getEventDocument();
    BaseObject subscriptObj = eventDoc.getXObject(getSubscriptionClassRef(), "subscriber",
        refDefaultSerializer.serialize(calDocRef), false);
    if (subscriptObj == null) {
      //for backwards compatibility
      subscriptObj = eventDoc.getXObject(getSubscriptionClassRef(), "subscriber",
          refLocalSerializer.serialize(calDocRef), false);
    }
    return subscriptObj;
  }

  private DocumentReference getSubscriptionClassRef() {
    return new DocumentReference(getContext().getDatabase(),
        CalendarService.SUBSCRIPTION_CLASS_SPACE,
        CalendarService.SUBSCRIPTION_CLASS_DOC);
  }

  public NavigationDetails getNavigationDetails(IEvent event, Calendar cal
      ) throws XWikiException {
    Date eventDate = event.getEventDate();
    LOGGER.debug("getNavigationDetails for [" + event + "] with date [" + eventDate + "]");
    if (eventDate == null) {
      LOGGER.error("getNavigationDetails failed because eventDate is null for ["
          + event.getDocumentReference() + "].");
      return null;
    }
    NavigationDetails navDetail = new NavigationDetails(eventDate, 0);
    int nb = 10;
    int eventIndex, start = 0;
    List<IEvent> events;
    boolean hasMore, notFound;
    do {
      events = getEvents_internal(cal.getDocumentReference(), start, nb, false, eventDate);
      hasMore = events.size() == nb;
      eventIndex = events.indexOf(event);
      notFound = eventIndex < 0;
      navDetail.setOffset(start + eventIndex);
      start = start + nb;
      nb = nb * 2;
    } while (notFound && hasMore);
    if (!notFound) {
      LOGGER.debug("getNavigationDetails: returning " + navDetail);
      return navDetail;
    }
    return null;
  }

  public IEvent getEvent(DocumentReference eventDocRef) {
    return new Event(eventDocRef);
  }

  void injectExecution(Execution execution) {
    this.execution = execution;
  }

  void injectCalService(ICalendarService calService) {
    this.calService = calService;
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  void injectEventSearch(IEventSearch eventSearch) {
    this.eventSearch = eventSearch;
  }

}
