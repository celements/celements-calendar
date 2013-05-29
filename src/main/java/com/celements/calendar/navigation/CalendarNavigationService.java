package com.celements.calendar.navigation;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.xpn.xwiki.XWikiException;

@Component("default")
public class CalendarNavigationService implements ICalendarNavigationService {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarNavigationService.class);

  private static final Date DATE_LOW = new Date(-62135773200000L);
  private static final Date DATE_HIGH = new Date(253402297140000L);

  @Requirement
  private IEventManager eventMgr;

  public NavigationDetails getNavigationDetails(Date startDate, int offset) {
    return new NavigationDetails(startDate, offset);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event) throws XWikiException {
    return getNavigationDetails(calConfigDocRef, event, null);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, EventSearchQuery query) throws XWikiException {
    LOGGER.debug("getNavigationDetails for '" + event + "'");
    Date eventDate = event.getEventDate();
    if (eventDate != null) {
      ICalendar cal = new Calendar(calConfigDocRef, false);
      cal.setStartDate(eventDate);
      int offset = 0;
      int nb = 10;
      int eventIndex, start = 0;
      List<IEvent> events;
      boolean hasMore, notFound;
      do {
        if (query == null) {
          events = eventMgr.getEventsInternal(cal, start, nb);
        } else {
          events = eventMgr.searchEvents(cal, query).getEventList(start, nb);
        }
        hasMore = events.size() == nb;
        eventIndex = events.indexOf(event);
        notFound = eventIndex < 0;
        offset = start + eventIndex;
        start = start + nb;
        nb = nb * 2;
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("getNavigationDetails: events '" + events + "'");
          LOGGER.debug("getNavigationDetails: index for event '" + eventIndex);
        }
      } while (notFound && hasMore);
      if (!notFound) {
        NavigationDetails navDetail = new NavigationDetails(cal.getStartDate(), offset);
        LOGGER.debug("getNavigationDetails: found '" + navDetail + "'");
        return navDetail;
      } else {
        LOGGER.debug("getNavigationDetails: not found");
      }
    } else {
      LOGGER.error("getNavigationDetails: eventDate is null for '" + event + "'");
    }
    return null;
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb) throws XWikiException {
    return getCalendarNavigation(calConfigDocRef,
        new NavigationDetails(eventDate, offset), nb);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb) throws XWikiException {
    ICalendar cal = getCalendar(calConfigDocRef, false, navDetails.getStartDate());
    ICalendar calArchive = getCalendar(calConfigDocRef, true, navDetails.getStartDate());
    int[] counts = getCounts((int) cal.getNrOfEvents(), (int) calArchive.getNrOfEvents(),
        navDetails.getOffset(), nb);

    CalendarNavigation calendarNavigation = new CalendarNavigation(
        counts[0], counts[1], counts[2],
        getStartNavDetails(cal, calArchive),
        getEndNavDetails(cal, calArchive, nb),
        getPrevNavDetails(cal, calArchive, navDetails, nb),
        getNextNavDetails(cal, navDetails, nb));
    LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
        + calConfigDocRef + "' and navDetails '" + navDetails + "'");
    return calendarNavigation;
  }

  private NavigationDetails getStartNavDetails(ICalendar cal, ICalendar calArchive) {
    NavigationDetails startNavDetails = null;
    if (calArchive.getNrOfEvents() > 0) {
      startNavDetails = new NavigationDetails(calArchive.getFirstEvent().getEventDate(), 0);
    } else if (cal.getNrOfEvents() > 0) {
      startNavDetails = new NavigationDetails(cal.getFirstEvent().getEventDate(), 0);
    }
    return startNavDetails;
  }

  private NavigationDetails getEndNavDetails(ICalendar cal, ICalendar calArchive, int nb)
      throws XWikiException {
    NavigationDetails endNavDetails = null;
    int endOffset = (int) cal.getNrOfEvents() - nb;
    if (endOffset >= 0) {
      endNavDetails = getFirstNavDetails(cal, endOffset);
    } else if (calArchive.getNrOfEvents() > 0) {
      endNavDetails = getLastNavDetails(calArchive, endOffset);
    }
    return endNavDetails;
  }

  private NavigationDetails getPrevNavDetails(ICalendar cal, ICalendar calArchive,
      NavigationDetails navDetails, int nb) throws XWikiException {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    if ((prevOffset >= 0) && (cal.getNrOfEvents() > 0)) {
      prevNavDetails = getFirstNavDetails(cal, prevOffset);
    } else if ((prevOffset < 0) && (calArchive.getNrOfEvents() > 0)) {
      prevNavDetails = getLastNavDetails(calArchive, prevOffset);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(ICalendar cal, NavigationDetails navDetails,
      int nb) throws XWikiException {
    NavigationDetails nextNavDetails;
    int nextOffset = navDetails.getOffset() + nb;
    if (cal.getNrOfEvents() > nextOffset) {
      nextNavDetails = getFirstNavDetails(cal, nextOffset);
    } else {
      nextNavDetails = navDetails;
    }
    return nextNavDetails;
  }

  private NavigationDetails getFirstNavDetails(ICalendar cal, int offset
      ) throws XWikiException {
    IEvent firstEvent = getFirstElement(cal.getEventsInternal(offset, 1));
    return getNavigationDetails(cal.getDocumentReference(), firstEvent);
  }

  private NavigationDetails getLastNavDetails(ICalendar cal, int offset
      ) throws XWikiException {
    IEvent lastEvent = getLastElement(cal.getEventsInternal(0, Math.abs(offset)));
    return getNavigationDetails(cal.getDocumentReference(), lastEvent);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query) throws XWikiException {
    EventSearchResult calResult = getCalendar(calDocRef, false,
        navDetails.getStartDate()).searchEvents(query);
    EventSearchResult calArchiveResult = getCalendar(calDocRef, true,
        navDetails.getStartDate()).searchEvents(query);
    EventSearchResult calAllResult = getCalendar(calDocRef, false, DATE_LOW
        ).searchEvents(query);
    EventSearchResult calAllArchiveResult = getCalendar(calDocRef, true, DATE_HIGH
        ).searchEvents(query);
    int[] counts = getCounts(calResult.getSize(), calArchiveResult.getSize(),
        navDetails.getOffset(), nb);

    CalendarNavigation calendarNavigation = new CalendarNavigation(
        counts[0], counts[1], counts[2],
        getStartNavDetails(calAllResult),
        getEndNavDetails(calDocRef, nb, query, calAllArchiveResult),
        getPrevNavDetails(calDocRef, navDetails, nb, query, calResult, calArchiveResult),
        getNextNavDetails(calDocRef, navDetails, nb, query, calResult));
    LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
        + calDocRef + "', navDetails '" + navDetails + "' and query '" + query + "'");
    return calendarNavigation;
  }

  private NavigationDetails getStartNavDetails(EventSearchResult calAllSearchResult) {
    NavigationDetails startNavDetails = null;
    if (calAllSearchResult.getSize() > 0) {
      IEvent startDate = getFirstElement(calAllSearchResult.getEventList(0, 1));
      startNavDetails = new NavigationDetails(startDate.getEventDate(), 0);
    }
    return startNavDetails;
  }

  private NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb,
      EventSearchQuery query, EventSearchResult calAllInvSearchResult
      ) throws XWikiException {
    NavigationDetails endNavDetails = null;
    if ((calAllInvSearchResult.getSize() > 0)) {
      endNavDetails = getLastNavDetails(calDocRef, nb, query, calAllInvSearchResult);
    }
    return endNavDetails;
  }

  private NavigationDetails getPrevNavDetails(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query, EventSearchResult
      calSearchResult, EventSearchResult calArchiveSearchResult) throws XWikiException {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    if ((prevOffset >= 0) && (calSearchResult.getSize() > 0)) {
      prevNavDetails = getFirstNavDetails(calDocRef, prevOffset, query, calSearchResult);
    } else if ((prevOffset < 0) && (calArchiveSearchResult.getSize() > 0)) {
      prevNavDetails = getLastNavDetails(calDocRef, prevOffset, query,
          calArchiveSearchResult);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query,
      EventSearchResult calSearchResult) throws XWikiException {
    NavigationDetails nextNavDetails;
    int nextOffset = navDetails.getOffset() + nb;
    if (calSearchResult.getSize() > nextOffset) {
      nextNavDetails = getFirstNavDetails(calDocRef, nextOffset, query, calSearchResult);
    } else {
      nextNavDetails = navDetails;
    }
    return nextNavDetails;
  }

  private NavigationDetails getFirstNavDetails(DocumentReference calDocRef, int offset,
      EventSearchQuery query, EventSearchResult searchResult) throws XWikiException {
    IEvent firstEvent = getFirstElement(searchResult.getEventList(offset, 1));
    return getNavigationDetails(calDocRef, firstEvent, query);
  }

  private NavigationDetails getLastNavDetails(DocumentReference calDocRef, int offset,
      EventSearchQuery query, EventSearchResult searchResult) throws XWikiException {
    IEvent lastEvent = getLastElement(searchResult.getEventList(0, Math.abs(offset)));
    return getNavigationDetails(calDocRef, lastEvent, query);
  }

  private int[] getCounts(int calSize, int calArchiveSize, int offset, int nb) {
    int[] counts = new int[3];
    counts[0] = calArchiveSize + offset;
    counts[1] = calSize - offset - nb;
    if (counts[1] < 0) {
      counts[1] = 0;
    }
    counts[2] = calSize + calArchiveSize;
    return counts;
  }

  private static ICalendar getCalendar(DocumentReference calConfigDocRef,
      boolean isArchive, Date startDate) {
    ICalendar cal = new Calendar(calConfigDocRef, isArchive);
    cal.setStartDate(startDate);
    return cal;
  }

  private static <T> T getFirstElement(List<T> list) {
    if ((list != null) && (list.size() > 0)) {
      return list.get(0);
    }
    return null;
  }

  private static <T> T getLastElement(List<T> list) {
    if ((list != null) && (list.size() > 0)) {
      return list.get(list.size() - 1);
    }
    return null;
  }

  void injectEventManager(IEventManager eventMgr) {
    this.eventMgr = eventMgr;
  }

}
