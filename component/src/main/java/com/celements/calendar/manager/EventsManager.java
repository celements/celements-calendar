package com.celements.calendar.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.engine.CalendarEngineLucene;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.calendar.tag.CalendarTag;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class EventsManager implements IEventManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventsManager.class);

  @Requirement
  private ICalendarService calService;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private ICalendarClassConfig calClassConf;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement(CalendarTag.CLASS_DEF_HINT)
  private ClassDefinition calendarTagClassDef;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public boolean addEvent(ICalendar calendar, DocumentReference eventDocRef)
      throws DocumentNotExistsException, DocumentSaveException {
    XWikiDocument eventDoc = modelAccess.getDocument(eventDocRef);
    if (isTaggingAvailable()) {
      XWikiObjectEditor editor = createTagObjectEditor(eventDoc, calendar);
      return !editor.fetch().exists() && (editor.createFirst() != null);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public boolean removeEvent(ICalendar calendar, DocumentReference eventDocRef)
      throws DocumentNotExistsException, DocumentSaveException {
    XWikiDocument eventDoc = modelAccess.getDocument(eventDocRef);
    if (isTaggingAvailable()) {
      XWikiObjectEditor editor = createTagObjectEditor(eventDoc, calendar);
      return editor.fetch().exists() && editor.deleteFirst().isPresent();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private boolean isTaggingAvailable() {
    // TODO Auto-generated method stub
    return false;
  }

  private XWikiObjectEditor createTagObjectEditor(XWikiDocument eventDoc, ICalendar calendar) {
    return XWikiObjectEditor.on(eventDoc).filter(calendarTagClassDef).filter(CalendarTag.FIELD_NAME,
        calendar);
  }

  @Override
  @Deprecated
  public List<EventApi> getEvents(ICalendar cal, int start, int nb) {
    List<EventApi> eventApiList = new ArrayList<>();
    for (IEvent event : getEventsInternal(cal, start, nb)) {
      eventApiList.add(new EventApi(event, cal.getLanguage(), getContext()));
    }
    return eventApiList;
  }

  @Override
  public List<IEvent> getAllEventsInternal(ICalendar cal) {
    return getEventsInternal(cal, 0, 0);
  }

  @Override
  public List<IEvent> getEventsInternal(ICalendar cal, int start, int nb) {
    List<IEvent> eventList = Collections.emptyList();
    try {
      eventList = cal.getEngine().getEvents(cal, start, nb);
      filterEventListForSubscription(cal.getDocumentReference(), eventList);
    } catch (XWikiException exc) {
      LOGGER.error("Error while getting events for '{}'", cal, exc);
    }
    LOGGER.debug("getEventsInternal: {} events found for cal '{}', start '{}' and nb '{}'",
        eventList.size(), cal, start, nb);
    return eventList;
  }

  // TODO it should be possible to include this in lucene query
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

  private boolean checkEventSubscription(DocumentReference calDocRef, IEvent event)
      throws XWikiException {
    return isHomeCalendar(calDocRef, event.getDocumentReference()) || isEventSubscribed(calDocRef,
        event);
  }

  boolean isHomeCalendar(DocumentReference calDocRef, DocumentReference eventDocRef)
      throws XWikiException {
    SpaceReference eventSpaceRef = calService.getEventSpaceRefForCalendar(calDocRef);
    boolean isHomeCal = eventDocRef.getLastSpaceReference().equals(eventSpaceRef);
    LOGGER.trace("isHomeCalendar: for '{}' check on cal '{}' with space '{}' returning {}",
        eventDocRef, calDocRef, eventSpaceRef, isHomeCal);
    return isHomeCal;
  }

  private boolean isEventSubscribed(DocumentReference calDocRef, IEvent event) {
    BaseObject obj = getSubscriptionObject(calDocRef, event);
    ICalendar calendar = event.getCalendar();
    BaseObject calObj = null;
    if ((calendar != null) && (calendar.getCalDoc() != null)) {
      calObj = calendar.getCalDoc().getXObject(calClassConf.getCalendarClassRef(
          calDocRef.getWikiReference()));
    }
    boolean isSubscribed = false;
    if ((obj != null) && (obj.getIntValue("doSubscribe") == 1) && (calObj != null)
        && (calObj.getIntValue("is_subscribable") == 1)) {
      isSubscribed = true;
    }
    LOGGER.trace("isEventSubscribed: for [" + event.getDocumentReference() + "] returning "
        + isSubscribed);
    return isSubscribed;
  }

  private BaseObject getSubscriptionObject(DocumentReference calDocRef, IEvent event) {
    XWikiDocument eventDoc = event.getEventDocument();
    WikiReference wikiRef = event.getDocumentReference().getWikiReference();
    BaseObject subscriptObj = eventDoc.getXObject(calClassConf.getSubscriptionClassRef(wikiRef),
        "subscriber", webUtilsService.serializeRef(calDocRef), false);
    if (subscriptObj == null) {
      // for backwards compatibility
      subscriptObj = eventDoc.getXObject(calClassConf.getSubscriptionClassRef(wikiRef),
          "subscriber", webUtilsService.serializeRef(calDocRef, true), false);
    }
    return subscriptObj;
  }

  @Override
  public EventSearchResult searchEvents(ICalendar cal, IEventSearchQuery query) {
    EventSearchResult eventsResult = getLuceneEngine(cal).searchEvents(cal, query);
    try {
      // XXX calling getSize() imediatelly is a dirty Workaround!!!
      // XXX accessing results directly prevents lucene inconsistancies
      // XXX if multiple results are created (e.g. in Navigation).
      int size = eventsResult.getSize();
      LOGGER.debug("searchEvents: {} events found for cal '{}' and query '{}'", size, cal, query);
    } catch (LuceneSearchException lse) {
      LOGGER.error("Unable to search for cal '{}'", cal, lse);
    }
    return eventsResult;
  }

  private CalendarEngineLucene getLuceneEngine(ICalendar cal) {
    ICalendarEngineRole engine = cal.getEngineWithoutLimitCheck();
    if (engine instanceof CalendarEngineLucene) {
      return (CalendarEngineLucene) engine;
    } else {
      throw new IllegalStateException("events can only be searched when lucene calendar "
          + "engine is activated");
    }
  }

  /**
   * @param calDoc
   * @param isArchive
   * @return
   * @deprecated instead use countEvents(DocumentReference, boolean)
   */
  @Override
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive) {
    return countEvents(calDoc, isArchive, new Date());
  }

  /**
   * @param calDoc
   * @param isArchive
   * @param startDate
   * @return
   * @deprecated instead use countEvents(DocumentReference, boolean, Date)
   */
  @Override
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate) {
    return countEvents(calDoc.getDocumentReference(), isArchive, startDate);
  }

  @Override
  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive) {
    return countEvents(calDocRef, isArchive, new Date());
  }

  @Override
  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive, Date startDate) {
    ICalendar cal = calService.getCalendarByCalRef(calDocRef, isArchive);
    cal.setStartDate(startDate);
    return countEvents(cal);
  }

  @Override
  public long countEvents(ICalendar cal) {
    return cal.getEngine().countEvents(cal);
  }

  @Override
  public IEvent getEvent(DocumentReference eventDocRef) {
    return new Event(eventDocRef);
  }

  @Override
  public IEvent getFirstEvent(ICalendar cal) {
    return cal.getEngine().getFirstEvent(cal);
  }

  @Override
  public IEvent getLastEvent(ICalendar cal) {
    return cal.getEngine().getLastEvent(cal);
  }

  void injectCalService(ICalendarService calService) {
    this.calService = calService;
  }

}
