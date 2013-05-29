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

  private NavigationDetails getStartNavDetails(ICalendar cal, ICalendar calArchive) {
    NavigationDetails startNavDetails = null;
    if (calArchive.getNrOfEvents() > 0) {
      IEvent startDate = calArchive.getFirstEvent();
      startNavDetails = new NavigationDetails(startDate.getEventDate(), 0);
    } else if (cal.getNrOfEvents() > 0) {
      IEvent startDate = cal.getFirstEvent();
      startNavDetails = new NavigationDetails(startDate.getEventDate(), 0);
    }
    return startNavDetails;
  }

  private NavigationDetails getEndNavDetails(ICalendar cal, ICalendar calArchive, int nb)
      throws XWikiException {
    NavigationDetails endNavDetails = null;
    int endOffset = (int) cal.getNrOfEvents() - nb;
    if (endOffset >= 0) {
      IEvent endEvent = getFirstElement(cal.getEventsInternal(endOffset, 1));
      endNavDetails = getNavigationDetails(cal.getDocumentReference(), endEvent);
    } else if (calArchive.getNrOfEvents() > 0) {
      IEvent endEvent = getLastElement(calArchive.getEventsInternal(0,
          Math.abs(endOffset)));
      endNavDetails = getNavigationDetails(cal.getDocumentReference(), endEvent);
    }
    return endNavDetails;
  }

  private NavigationDetails getPrevNavDetails(ICalendar cal, ICalendar calArchive,
      NavigationDetails navDetails, int nb) throws XWikiException {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    if (prevOffset >= 0) {
      if (cal.getNrOfEvents() > 0) {
        IEvent prevEvent = getFirstElement(cal.getEventsInternal(prevOffset, 1));
        prevNavDetails = getNavigationDetails(cal.getDocumentReference(), prevEvent);
      }
    } else if (calArchive.getNrOfEvents() > 0) {
      IEvent prevEvent = getLastElement(calArchive.getEventsInternal(0,
          Math.abs(prevOffset)));
      prevNavDetails = getNavigationDetails(cal.getDocumentReference(), prevEvent);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(ICalendar cal,
      NavigationDetails navDetails, int nb) throws XWikiException {
    NavigationDetails nextNavDetails;
    int nextOffset = navDetails.getOffset() + nb;
    if (cal.getNrOfEvents() > nextOffset) {
      IEvent nextEvent = getFirstElement(cal.getEventsInternal(nextOffset, 1));
      nextNavDetails = getNavigationDetails(cal.getDocumentReference(), nextEvent);
    } else {
      nextNavDetails = navDetails;
    }
    return nextNavDetails;
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query) throws XWikiException {
    ICalendar cal = getCalendar(calConfigDocRef, false, navDetails.getStartDate());
    ICalendar calArchive = getCalendar(calConfigDocRef, true, navDetails.getStartDate());
    EventSearchResult calSearchResult = cal.searchEvents(query);
    EventSearchResult calArchiveSearchResult = calArchive.searchEvents(query);
    int[] counts = getCounts(calSearchResult.getSize(), calArchiveSearchResult.getSize(),
        navDetails.getOffset(), nb);
    CalendarNavigation calendarNavigation = new CalendarNavigation(
        counts[0], counts[1], counts[2],
        null, null, null, null); //TODO
    LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
        + calConfigDocRef + "', navDetails '" + navDetails + "' and query '" + query + "'");
    return calendarNavigation;
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
