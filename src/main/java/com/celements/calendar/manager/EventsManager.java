package com.celements.calendar.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.calendar.service.ICalendarService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("default")
public class EventsManager implements IEventManager {

  @Requirement
  Execution execution;

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      EventsManager.class);

  @Requirement
  ICalendarService calService;

  //TODO we must change to 'default' serializer with the wikiname included
  @Requirement("local")
  private EntityReferenceSerializer<String> refLocalSerializer;

  @Requirement("default")
  private EntityReferenceSerializer<String> refDefaultSerializer;

  @Requirement
  EntityReferenceResolver<String> stringRefResolver;

  @Requirement QueryManager queryManager;

  public EventsManager() {}

  public List<EventApi> getEvents(ICalendar cal, int start, int nb) {
    List<EventApi> eventApiList = new ArrayList<EventApi>();
    try {
      for (Event theEvent : getEvents_internal(cal.getCalDoc(), start, nb,
          cal.isArchive(), cal.getStartDate())) {
        eventApiList.add(new EventApi(theEvent, cal.getLanguage(), getContext()));
      }
    } catch (XWikiException e) {
      mLogger.error(e);
    } catch (QueryException exp) {
      mLogger.error("Failed to exequte getEvents query.", exp);
    }
    return eventApiList;
  }

  private List<Event> getEvents_internal(XWikiDocument calDoc, int start, int nb,
      boolean isArchive, Date startDate) throws QueryException, XWikiException {
    List<Event> eventList = new ArrayList<Event>();
    List<String> eventDocs = queryManager.createQuery(getQuery(calDoc, isArchive,
        startDate, false), Query.HQL).setOffset(start).setLimit(nb).execute();
    mLogger.debug(eventDocs.size() + " events found. " + eventDocs);
    for (String eventDocName : eventDocs) {
      Event theEvent = new Event(getDocRefFromFullName(eventDocName), getContext());
      if(checkEventSubscription(calDoc.getDocumentReference(), theEvent)){
        mLogger.debug("getEvents: add to result " + eventDocName);
        eventList.add(theEvent);
      } else {
        mLogger.debug("getEvents: skipp " + eventDocName);
      }
    }
    return eventList;
  }

  private DocumentReference getDocRefFromFullName(String eventDocName) {
    DocumentReference eventRef = new DocumentReference(stringRefResolver.resolve(
        eventDocName, EntityType.DOCUMENT));
    eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    mLogger.debug("getDocRefFromFullName: for [" + eventDocName + "] got reference ["
        + eventRef + "].");
    return eventRef;
  }

  public long countEvents(XWikiDocument calDoc, boolean isArchive) {
    return countEvents(calDoc, isArchive, new Date());
  }

  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate) {
    List<Object> eventCount = null;
    try {
      eventCount = queryManager.createQuery(getQuery(calDoc, isArchive, startDate, true),
          Query.HQL).execute();
    } catch (XWikiException e) {
      mLogger.error("Exception while counting number of events for calendar '" + 
          ((calDoc != null)?calDoc.getDocumentReference():calDoc) + "'", e);
    } catch (QueryException exp) {
      mLogger.error("Failed to exequte countEvents.", exp);
    }
    if((eventCount != null) && (eventCount.size() > 0)) {
      mLogger.debug("Count resulted in " + eventCount.get(0) + " which is of class " +
          eventCount.get(0).getClass());
      return (Long)eventCount.get(0);
    }
    return 0;
  }

    private String getQuery(XWikiDocument calDoc, boolean isArchive, Date startDate,
        boolean count) throws XWikiException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    String timeComp = ">=";
    String sortOrder = "asc";
    String selectEmptyDates = "or ec.eventDate is null";
    if(isArchive){
      timeComp = "<";
      sortOrder = "desc";
      selectEmptyDates = "";
    }
    String hql = "select ";
    if(count){
      hql += "count(obj.name)";
    } else {
      hql += "obj.name";
    }
    hql += " from XWikiDocument doc, BaseObject as obj, ";
    hql += CelementsCalendarPlugin.CLASS_EVENT + " as ec ";
    hql += "where doc.fullName = obj.name and doc.translation = 0 and ec.id.id=obj.id ";
    VelocityContext vcontext = ((VelocityContext) getContext().get("vcontext"));
    String defaultLanguage = (String)vcontext.get("default_language");
    hql += "and ec.lang='" + defaultLanguage + "' ";
    hql += "and (ec.eventDate " + timeComp + " '"
      + format.format(getMidnightDate(startDate)) + "' " + selectEmptyDates + ") and ";
    hql += calService.getAllowedSpacesHQL(calDoc);
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    hql += ", ec.l_title " + sortOrder;
    mLogger.debug(hql);
    
    return hql;
  }

  /**
   * getMidnightDate
   * @param startDate 
   * 
   * @param startDate may not be null
   * @return
   */
  private Date getMidnightDate(Date startDate) {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(startDate);
    cal.set(java.util.Calendar.HOUR, 0);
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    Date dateMidnight = cal.getTime();
    mLogger.debug("date is: " + dateMidnight);
    return dateMidnight;
  }

  private boolean checkEventSubscription(DocumentReference calDocRef, Event theEvent
      ) throws XWikiException {
    return isHomeCalendar(calDocRef, theEvent)
        || isEventSubscribed(calDocRef, theEvent);
  }

  boolean isHomeCalendar(DocumentReference calDocRef, Event theEvent
      ) throws XWikiException {
    String eventSpaceForCal = calService.getEventSpaceForCalendar(calDocRef);
    boolean isHomeCal = theEvent.getDocumentReference().getLastSpaceReference().getName(
        ).equals(eventSpaceForCal);
    mLogger.trace("isHomeCalendar: for [" + theEvent.getDocumentReference()
        + "] check on calDocRef [" + calDocRef + "] with space [" + eventSpaceForCal
        + "] returning " + isHomeCal);
    return isHomeCal;
  }
  
  private boolean isEventSubscribed(DocumentReference calDocRef, Event theEvent
      ) throws XWikiException {
    BaseObject obj = getSubscriptionObject(calDocRef, theEvent);

    ICalendar calendar = theEvent.getCalendar(getContext());
    BaseObject calObj = null;
    if ((calendar != null) && (calendar.getCalDoc() != null)){
      calObj = calendar.getCalDoc().getXObject(getCalenderConfigClassRef());
    }
    boolean isSubscribed = false;
    if((obj != null) && (obj.getIntValue("doSubscribe") == 1)
        && (calObj != null) && (calObj.getIntValue("is_subscribable") == 1)){
      isSubscribed = true;
    }
    mLogger.trace("isEventSubscribed: for [" + theEvent.getDocumentReference()
        + "] returning " + isSubscribed);
    return isSubscribed;
  }

  private DocumentReference getCalenderConfigClassRef() {
    return new DocumentReference(getContext().getDatabase(),
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE,
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
  }

  private BaseObject getSubscriptionObject(DocumentReference calDocRef, Event event) {
    BaseObject subscriptObj = event.getEventDocument().getXObject(
        getSubscriptionClassRef(), "subscriber", refDefaultSerializer.serialize(
            calDocRef), false);
    if (subscriptObj == null) {
      //for backwards compatibility
      subscriptObj = event.getEventDocument().getXObject(getSubscriptionClassRef(),
          "subscriber", refLocalSerializer.serialize(calDocRef), false);
    }
    return subscriptObj;
  }

  private DocumentReference getSubscriptionClassRef() {
    return new DocumentReference(getContext().getDatabase(),
        CelementsCalendarPlugin.SUBSCRIPTION_CLASS_SPACE,
        CelementsCalendarPlugin.SUBSCRIPTION_CLASS_DOC);
  }
  
  public NavigationDetails getNavigationDetails(Event theEvent, Calendar cal
      ) throws XWikiException {
    mLogger.debug("getNavigationDetails for [" + theEvent + "] with date ["
        + theEvent.getEventDate() + "]");
    try {
      NavigationDetails navDetail = new NavigationDetails(theEvent.getEventDate(), 0);
      int nb = 10;
      int eventIndex, start = 0;
      List<Event> events;
      boolean hasMore, notFound;
      do {
        events = getEvents_internal(cal.getCalDoc(), start, nb, false,
            theEvent.getEventDate());
        hasMore = events.size() == nb;
        eventIndex = events.indexOf(theEvent);
        notFound = eventIndex < 0;
        navDetail.setOffset(start + eventIndex);
        start = start + nb;
        nb = nb * 2;
      } while (notFound && hasMore);
      if (!notFound) {
        mLogger.debug("getNavigationDetails: returning " + navDetail);
        return navDetail;
      }
    } catch (QueryException qExp) {
      mLogger.error("getNavigationDetails: Failed to get events.", qExp);
    }
    return null;
  }

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

}
