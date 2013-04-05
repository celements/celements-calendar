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
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.service.CalendarService;
import com.celements.calendar.service.ICalendarService;
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
		DocumentReference calDocRef = cal.getDocumentReference();
		try {
			return getEvents_internal(cal.getEngine(), calDocRef, cal.getStartDate(),
					cal.isArchive(), webUtilsService.getDefaultLanguage(),
					calService.getAllowedSpaces(calDocRef), start, nb);
		} catch (XWikiException exc) {
			LOGGER.error("Exception while getting events for calendar '" + calDocRef + "'", exc);
		}
		return Collections.emptyList();
	}

	private List<IEvent> getEvents_internal(ICalendarEngineRole engine,
			DocumentReference calDocRef, Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces, int start, int nb) throws XWikiException {
		List<IEvent> eventList = engine.getEvents(startDate, isArchive, lang, allowedSpaces,
				start, nb);
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

	private boolean isEventSubscribed(DocumentReference calDocRef, IEvent event) {
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

	private BaseObject getSubscriptionObject(DocumentReference calDocRef, IEvent event) {
		XWikiDocument eventDoc = event.getEventDocument();
		BaseObject subscriptObj = eventDoc.getXObject(getSubscriptionClassRef(), "subscriber",
				webUtilsService.getRefDefaultSerializer().serialize(calDocRef), false);
		if (subscriptObj == null) {
			//for backwards compatibility
			subscriptObj = eventDoc.getXObject(getSubscriptionClassRef(), "subscriber",
					webUtilsService.getRefLocalSerializer().serialize(calDocRef), false);
		}
		return subscriptObj;
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
		DocumentReference calDocRef = cal.getDocumentReference();
		boolean isArchive = cal.isArchive();
		Date startDate = cal.getStartDate();
		String calFullName = webUtilsService.getRefDefaultSerializer().serialize(calDocRef);
		String cacheKey = "EventsManager.countEvents|" + calFullName + "|" + isArchive + "|"
				+ startDate.getTime();
		Object cachedCount = execution.getContext().getProperty(cacheKey);
		if (cachedCount != null) {
			LOGGER.debug("Cached event count: " + cachedCount);
			return (Long) cachedCount;
		} else {
			try {
				long count = cal.getEngine().countEvents(startDate, isArchive,
						webUtilsService.getDefaultLanguage(), calService.getAllowedSpaces(calDocRef));
				LOGGER.debug("Event count for calendar '" + calDocRef + "' with startDate + '"
						+ startDate + "' and isArchive '" + isArchive + "': " + count);
				if (count > 0) {
					execution.getContext().setProperty(cacheKey, count);
				}
				return count;
			} catch (XWikiException exc) {
				LOGGER.error("Exception while counting events for calendar '" + calDocRef + "'",
						exc);
			}
		}
		return 0;
	}

	public NavigationDetails getNavigationDetails(IEvent event, ICalendar cal
			) throws XWikiException {
		Date eventDate = event.getEventDate();
		LOGGER.debug("getNavigationDetails for [" + event + "] with date [" + eventDate + "]");
		if (eventDate == null) {
			LOGGER.error("getNavigationDetails failed because eventDate is null for ["
					+ event.getDocumentReference() + "].");
			return null;
		}
		DocumentReference calDocRef = cal.getDocumentReference();
		String lang = webUtilsService.getDefaultLanguage();
		List<String> allowedSpaces = calService.getAllowedSpaces(calDocRef);
		NavigationDetails navDetail = new NavigationDetails(eventDate, 0);
		int nb = 10;
		int eventIndex, start = 0;
		List<IEvent> events;
		boolean hasMore, notFound;
		do {
			events = getEvents_internal(cal.getEngine(), calDocRef, eventDate, false, lang,
					allowedSpaces, start, nb);
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

	private DocumentReference getCalenderConfigClassRef() {
		return new DocumentReference(getContext().getDatabase(),
				CalendarService.CLASS_CALENDAR_SPACE,
				CalendarService.CLASS_CALENDAR_DOC);
	}

	private DocumentReference getSubscriptionClassRef() {
		return new DocumentReference(getContext().getDatabase(),
				CalendarService.SUBSCRIPTION_CLASS_SPACE,
				CalendarService.SUBSCRIPTION_CLASS_DOC);
	}

	void injectExecution(Execution execution) {
		this.execution = execution;
	}

	void injectCalService(ICalendarService calService) {
		this.calService = calService;
	}

}
