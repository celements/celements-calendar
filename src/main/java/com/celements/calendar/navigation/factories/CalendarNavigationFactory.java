package com.celements.calendar.navigation.factories;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationFactory implements ICalendarNavigationFactory {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      NavigationDetailsFactory.class);

  private static final Date DATE_LOW = new Date(-62135773200000L);
  private static final Date DATE_HIGH = new Date(253402297140000L);

  private IEventManager eventMgr;

  private INavigationDetailsFactory navDetailsFactory;

  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb) {
    ICalendar cal = getCalendar(calDocRef, false, navDetails.getStartDate());
    ICalendar calArchive = getCalendar(calDocRef, true, navDetails.getStartDate());
    UncertainCount[] counts = getCounts((int) getEventMgr().countEvents(cal),
        (int) getEventMgr().countEvents(calArchive), navDetails.getOffset(), nb, false);

    CalendarNavigation calendarNavigation = new CalendarNavigation(
        counts[0], counts[1], counts[2],
        getStartNavDetails(calDocRef),
        getEndNavDetails(calDocRef, nb),
        getPrevNavDetails(cal, calArchive, navDetails, nb),
        getNextNavDetails(cal, navDetails, nb));
    LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
        + calDocRef+ "' and navDetails '" + navDetails + "'");
    return calendarNavigation;
  }

  NavigationDetails getStartNavDetails(DocumentReference calDocRef) {
    ICalendar calAll = getCalendar(calDocRef, false, DATE_LOW);
    if (getEventMgr().countEvents(calAll) > 0) {
      return new NavigationDetails(getEventMgr().getFirstEvent(calAll).getEventDate(), 0);
    }
    return null;
  }

  NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb) {
    ICalendar calAll = getCalendar(calDocRef, false, DATE_LOW);
    int countAll = (int) getEventMgr().countEvents(calAll);
    if (countAll > 0) {
      int endOffset = countAll - nb;
      return getFirstNavDetails(calAll, endOffset > 0 ? endOffset : 0);
    }
    return null;
  }

  private NavigationDetails getPrevNavDetails(ICalendar cal, ICalendar calArchive,
      NavigationDetails navDetails, int nb) {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    if ((prevOffset >= 0) && (getEventMgr().countEvents(cal) > 0)) {
      prevNavDetails = getFirstNavDetails(cal, prevOffset);
    } else if ((prevOffset < 0) && (getEventMgr().countEvents(calArchive) > 0)) {
      prevNavDetails = getLastNavDetails(calArchive, prevOffset);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(ICalendar cal, NavigationDetails navDetails,
      int nb) {
    NavigationDetails nextNavDetails;
    int nextOffset = navDetails.getOffset() + nb;
    if (getEventMgr().countEvents(cal) > nextOffset) {
      nextNavDetails = getFirstNavDetails(cal, nextOffset);
    } else {
      nextNavDetails = navDetails;
    }
    return nextNavDetails;
  }

  private NavigationDetails getFirstNavDetails(ICalendar cal, int offset) {
    IEvent firstEvent = getFirstElement(getEventMgr().getEventsInternal(cal, offset, 1));
    return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(),
        firstEvent);
  }

  private NavigationDetails getLastNavDetails(ICalendar cal, int offset) {
    offset = Math.abs(offset);
    IEvent lastEvent = getLastElement(getEventMgr().getEventsInternal(cal, 0, offset));
    return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(),
        lastEvent);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query) {
    EventSearchResult calResult = getCalendar(calDocRef, false,
        navDetails.getStartDate()).searchEvents(query);
    EventSearchResult calArchiveResult = getCalendar(calDocRef, true,
        navDetails.getStartDate()).searchEvents(query);
    EventSearchResult calAllResult = getCalendar(calDocRef, false, DATE_LOW
        ).searchEvents(query);
    EventSearchResult calAllArchiveResult = getCalendar(calDocRef, true, DATE_HIGH
        ).searchEvents(query);
    UncertainCount[] counts = getCounts(calResult.getSize(), calArchiveResult.getSize(),
        navDetails.getOffset(), nb, query != null);

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
      EventSearchQuery query, EventSearchResult calAllInvSearchResult) {
    NavigationDetails endNavDetails = null;
    if ((calAllInvSearchResult.getSize() > 0)) {
      endNavDetails = getLastNavDetails(calDocRef, nb, query, calAllInvSearchResult);
    }
    return endNavDetails;
  }

  private NavigationDetails getPrevNavDetails(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query, EventSearchResult
      calSearchResult, EventSearchResult calArchiveSearchResult) {
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
      EventSearchResult calSearchResult) {
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
      EventSearchQuery query, EventSearchResult searchResult) {
    IEvent firstEvent = getFirstElement(searchResult.getEventList(offset, 1));
    return getNavDetailsFactory().getNavigationDetails(calDocRef, firstEvent, query);
  }

  private NavigationDetails getLastNavDetails(DocumentReference calDocRef, int offset,
      EventSearchQuery query, EventSearchResult searchResult) {
    IEvent lastEvent = getLastElement(searchResult.getEventList(0, Math.abs(offset)));
    return getNavDetailsFactory().getNavigationDetails(calDocRef, lastEvent, query);
  }

  private UncertainCount[] getCounts(int calSize, int calArchiveSize, int offset, int nb,
      boolean isSearch) {
    UncertainCount[] counts = new UncertainCount[3];
    counts[0] = new UncertainCount(calArchiveSize + offset, isSearch
        && (calArchiveSize >= 1000));
    counts[1] = new UncertainCount(calSize - offset - nb, isSearch
        && (calSize >= 1000));
    counts[2] = new UncertainCount(calSize + calArchiveSize, isSearch
        && ((calSize >= 1000) || (calArchiveSize >= 1000)));
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

  private IEventManager getEventMgr() {
    if (eventMgr == null) {
      eventMgr = Utils.getComponent(IEventManager.class);
    }
    return eventMgr;
  }

  void injectEventMgr(IEventManager eventMgr) {
    this.eventMgr = eventMgr;
  }

  private INavigationDetailsFactory getNavDetailsFactory() {
    if (navDetailsFactory == null) {
      navDetailsFactory = new NavigationDetailsFactory();
    }
    return navDetailsFactory;
  }

  void injectNavDetailsFactory(INavigationDetailsFactory navDetailsFactory) {
    this.navDetailsFactory = navDetailsFactory;
  }

}
