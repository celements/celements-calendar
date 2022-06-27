package com.celements.calendar.navigation.factories;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IDateEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.IEventSearchRole;
import com.celements.calendar.service.ICalendarService;
import com.celements.performance.BenchmarkRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationFactory implements ICalendarNavigationFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CalendarNavigationFactory.class);

  private ICalendarService calService;
  private IEventSearchRole eventSearchService;
  private INavigationDetailsFactory navDetailsFactory;
  private ILuceneSearchService searchService;

  private BenchmarkRole benchService;

  /**
   * @deprecated instead use
   *             {@link #getCalendarNavigation(DocumentReference, NavigationDetails, int, boolean)
   *             and specify if a empty return page is needed or not
   */
  @Deprecated
  @Override
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb) {
    return getCalendarNavigation(calDocRef, navDetails, nb, false);
  }

  @Override
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, boolean isSendingEmptyPage) {
    ICalendar cal = getCalService().getCalendar(calDocRef, navDetails.getStartDate());
    NavigationDetails startNavDetails = null;
    NavigationDetails endNavDetails = null;
    try {
      getBenchService().bench("getCalendarNavigation after getCalendar");
      startNavDetails = getStartNavDetails(calDocRef);
      getBenchService().bench("getCalendarNavigation after getStartNavDetails");
      endNavDetails = getEndNavDetails(calDocRef, nb);
      getBenchService().bench("getCalendarNavigation after getEndNavDetails");
      if (isInvalidNavDetails(navDetails, cal)) {
        LOGGER.debug("isInvalidNavDetails true for '{}'", navDetails);
        navDetails = endNavDetails;
        cal = getCalService().getCalendar(calDocRef, navDetails.getStartDate());
      } else {
        LOGGER.debug("isInvalidNavDetails false for '{}'", navDetails);
      }
    } catch (NavigationDetailException exc) {
      LOGGER.warn("getCalendarNavigation encountered NavDetailException for calDocRef [" + calDocRef
          + "] and nb [" + nb + "] cal.getNrOfEvents [" + cal.getNrOfEvents() + "].", exc);
    }
    ICalendar calArchive = getCalService().getCalendarArchive(calDocRef, navDetails.getStartDate());
    UncertainCount[] counts = getCounts(cal.getNrOfEvents(), calArchive.getNrOfEvents(), navDetails
        .getOffset(), nb, false);

    CalendarNavigation calendarNavigation = new CalendarNavigation(counts[0], counts[1], counts[2],
        navDetails, startNavDetails, endNavDetails, getPrevNavDetails(cal, calArchive, navDetails,
            nb),
        getNextNavDetails(cal, navDetails, nb));
    if ((calendarNavigation.getCountAfter().getCount() <= 0) && (navDetails.getOffset() <= 0)
        && isSendingEmptyPage) {
      calendarNavigation = getEmptyCalendarNavigation(navDetails.getOffset());
    }
    LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '" + calDocRef
        + "' and navDetails '" + navDetails + "'");
    return calendarNavigation;
  }

  private boolean isInvalidNavDetails(NavigationDetails navDetails, ICalendar cal) {
    return (cal.getEventsInternal(navDetails.getOffset(), 1).size() == 0) && !getAllCalendar(cal)
        .isEmpty();
  }

  NavigationDetails getStartNavDetails(DocumentReference calDocRef)
      throws NavigationDetailException {
    LOGGER.debug("getStartNavDetails for calDocRef [{}]", calDocRef);
    ICalendar allCalendar = getAllCalendar(calDocRef);
    getBenchService().bench("getStartNavDetails after allCalendar");
    IEvent firstEvent = allCalendar.getFirstEvent();
    getBenchService().bench("getStartNavDetails after getFirstEvent");
    if (firstEvent != null) {
      Date startDate = firstEvent.getEventDate();
      if (startDate == null) {
        startDate = ICalendarClassConfig.DATE_LOW;
      }
      NavigationDetails navigationDetails = getNavDetailsFactory().getNavigationDetails(startDate,
          0);
      getBenchService().bench("getStartNavDetails after getNavDetailsFactory.getNavigationDetails");
      return navigationDetails;
    } else {
      throw new NavigationDetailException("empty calendar '" + calDocRef + "'");
    }
  }

  NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb)
      throws NavigationDetailException {
    LOGGER.debug("getEndNavDetails for calDocRef [" + calDocRef + "] and nb [" + nb + "].");
    ICalendar calAll = getAllCalendar(calDocRef);
    long countAll = calAll.getNrOfEvents();
    if (countAll > 0) {
      int endOffset = (int) (countAll - nb);
      LOGGER.debug("getEndNavDetails: get reverse calendar list to computing end. " + "endOffset ["
          + endOffset + "], countAll [" + countAll + "] nb [" + nb + "].");
      return getFirstNavDetails(calAll, endOffset > 0 ? endOffset : 0);
    }
    throw new NavigationDetailException("getEndNavDetails failes on empty calendar" + " part ["
        + calDocRef + "] nb [" + nb + "].");
  }

  private NavigationDetails getPrevNavDetails(ICalendar cal, ICalendar calArchive,
      NavigationDetails navDetails, int nb) {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    LOGGER.debug("getPrevNavDetails for calDocRef [" + cal.getDocumentReference() + "] and nb ["
        + nb + "] prevOffset [" + prevOffset + "].");
    try {
      if ((prevOffset >= 0) && !cal.isEmpty()) {
        prevNavDetails = getFirstNavDetails(cal, prevOffset);
      } else if ((prevOffset < 0) && !calArchive.isEmpty()) {
        prevNavDetails = getLastNavDetails(calArchive, prevOffset);
      }
    } catch (NavigationDetailException exc) {
      LOGGER.warn("getPrevNavDetails encountered NavDetailException for cal [" + cal
          .getDocumentReference() + "] and nb [" + nb + "] prevOffset [" + prevOffset + "].", exc);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(ICalendar cal, NavigationDetails navDetails, int nb) {
    NavigationDetails nextNavDetails = navDetails;
    int nextOffset = navDetails.getOffset() + nb;
    LOGGER.debug("getNextNavDetails for calDocRef [" + cal.getDocumentReference() + "] and nb ["
        + nb + "] nextOffset [" + nextOffset + "].");
    try {
      if (cal.getNrOfEvents() > nextOffset) {
        nextNavDetails = getFirstNavDetails(cal, nextOffset);
      }
    } catch (NavigationDetailException exc) {
      LOGGER.warn("getNextNavDetails encountered NavDetailException for calDocRef [" + cal
          .getDocumentReference() + "] and nb [" + nb + "] nextOffset [" + nextOffset + "].", exc);
    }
    return nextNavDetails;
  }

  private NavigationDetails getFirstNavDetails(ICalendar cal, int offset)
      throws NavigationDetailException {
    LOGGER.debug("getFirstNavDetails for cal [" + cal.getDocumentReference() + "] and offset ["
        + offset + "].");
    IEvent firstEvent = getFirstElement(cal.getEventsInternal(offset, 1));
    return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(), firstEvent);
  }

  private NavigationDetails getLastNavDetails(ICalendar cal, int offset)
      throws NavigationDetailException {
    LOGGER.debug("getLastNavDetails for cal [" + cal.getDocumentReference() + "] and offset ["
        + offset + "].");
    offset = Math.abs(offset);
    IEvent lastEvent = getLastElement(cal.getEventsInternal(0, offset));
    return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(), lastEvent);
  }

  /**
   * @deprecated instead use
   *             {@link #getCalendarNavigation(DocumentReference, NavigationDetails, int,
   *             IDateEventSearchQuery, boolean)
   *             and specify if a empty return page is needed or not
   */
  @Deprecated
  @Override
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, IDateEventSearchQuery query)
      throws LuceneSearchException {
    return getCalendarNavigation(calDocRef, navDetails, nb, query, false);
  }

  @Override
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, IDateEventSearchQuery query, boolean isSendingEmptyPage)
      throws LuceneSearchException {
    int actualOffset = navDetails.getOffset();
    EventSearchResult calAllResult = getAllCalendar(calDocRef).searchEvents(query);
    EventSearchResult calResult = getCalService().getCalendar(calDocRef, navDetails.getStartDate())
        .searchEvents(query);
    NavigationDetails startNavDetails = null;
    NavigationDetails endNavDetails = null;
    try {
      startNavDetails = getStartNavDetails(calAllResult);
      endNavDetails = getEndNavDetails(calDocRef, nb, query);
      int check = checkInvalidNavDetails(navDetails, query.getFromDate(), calResult, calAllResult);
      LOGGER.debug("checkInvalidNavDetails is '" + check + "' for '" + navDetails
          + "', calResult.getSize [" + calResult.getSize() + "], startNavDetail [" + startNavDetails
          + "], endNavDetails [" + endNavDetails + "].");
      if (check != 0) {
        navDetails = check > 0 ? endNavDetails : startNavDetails;
        LOGGER.trace("navDetails FIXED [" + navDetails + "].");
        calResult = getCalService().getCalendar(calDocRef, navDetails.getStartDate()).searchEvents(
            query);
      }
    } catch (NavigationDetailException exc) {
      LOGGER.info("getCalendarNavigation encountered NavDetailException for calDocRef [" + calDocRef
          + "] and nb [" + nb + "] calResult.getSize [" + calResult.getSize() + "].", exc);
    }
    EventSearchResult calArchiveResult = getCalService().getCalendarArchive(calDocRef, navDetails
        .getStartDate()).searchEvents(query);
    UncertainCount[] counts = getCounts(calResult.getSize(), calArchiveResult.getSize(), navDetails
        .getOffset(), nb, query != null);

    NavigationDetails nextNavDetails = getNextNavDetails(calDocRef, navDetails, nb, query,
        calResult);
    NavigationDetails prevNavDetails = getPrevNavDetails(calDocRef, navDetails, nb, query,
        calResult, calArchiveResult);
    CalendarNavigation calendarNavigation = null;
    calendarNavigation = new CalendarNavigation(counts[0], counts[1], counts[2], navDetails,
        startNavDetails, endNavDetails, prevNavDetails, nextNavDetails);
    if ((calendarNavigation.getCountAfter().getCount() <= 0) && (navDetails.getOffset() <= 0)
        && (actualOffset > 0) && isSendingEmptyPage) {
      calendarNavigation = getEmptyCalendarNavigation(actualOffset);
    }
    return calendarNavigation;
  }

  CalendarNavigation getEmptyCalendarNavigation(int actualOffset) {
    try {
      return new CalendarNavigation(
          new UncertainCount(0, false),
          new UncertainCount(0, false),
          new UncertainCount(0, false),
          NavigationDetails.create(ICalendarClassConfig.DATE_HIGH, actualOffset),
          NavigationDetails.create(ICalendarClassConfig.DATE_HIGH, actualOffset),
          NavigationDetails.create(ICalendarClassConfig.DATE_HIGH, actualOffset),
          NavigationDetails.create(ICalendarClassConfig.DATE_HIGH, actualOffset),
          NavigationDetails.create(ICalendarClassConfig.DATE_HIGH, actualOffset));
    } catch (NavigationDetailException exc) {
      throw new RuntimeException("Error in getEmptyCalendarNavigation: This error should "
          + "never happen, because its been set with default values", exc);
    }
  }

  private int checkInvalidNavDetails(NavigationDetails navDetails, Date fromDate,
      EventSearchResult calResult, EventSearchResult calAllResult) throws LuceneSearchException {
    if ((fromDate != null) && navDetails.getStartDate().before(fromDate)) {
      return -1;
    } else if ((calResult.getEventList(navDetails.getOffset(), 1).size() == 0) && (calAllResult
        .getSize() > 0)) {
      return 1;
    }
    return 0;
  }

  NavigationDetails getStartNavDetails(EventSearchResult calAllResult)
      throws NavigationDetailException, LuceneSearchException {
    if (calAllResult.getSize() > 0) {
      Date startDate = getFirstElement(calAllResult.getEventList(0, 1)).getEventDate();
      if (startDate == null) {
        startDate = ICalendarClassConfig.DATE_LOW;
      }
      return getNavDetailsFactory().getNavigationDetails(startDate, 0);
    } else {
      LOGGER.info("getStartNavDetails called with empty calendar.");
      throw new NavigationDetailException("getStartNavDetails called with empty" + " calendar.");
    }
  }

  NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb, IEventSearchQuery query)
      throws NavigationDetailException, LuceneSearchException {
    EventSearchResult calAllArchiveResult = getAllCalendarReversed(calDocRef).searchEvents(query);
    if ((calAllArchiveResult.getSize() > 0)) {
      return getLastNavDetails(calDocRef, nb, query, calAllArchiveResult);
    } else {
      LOGGER.info("getEndNavDetails called with empty calendar.");
      throw new NavigationDetailException("getEndNavDetails called with empty" + " calendar.");
    }
  }

  private NavigationDetails getPrevNavDetails(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, IEventSearchQuery query,
      EventSearchResult calSearchResult, EventSearchResult calArchiveSearchResult)
      throws LuceneSearchException {
    NavigationDetails prevNavDetails = null;
    int prevOffset = navDetails.getOffset() - nb;
    try {
      if ((prevOffset >= 0) && (calSearchResult.getSize() > 0)) {
        prevNavDetails = getFirstNavDetails(calDocRef, prevOffset, query, calSearchResult);
      } else if ((prevOffset < 0) && (calArchiveSearchResult.getSize() > 0)) {
        prevNavDetails = getLastNavDetails(calDocRef, prevOffset, query, calArchiveSearchResult);
      }
    } catch (NavigationDetailException exc) {
      LOGGER.warn("getPrevNavDetails encountered NavDetailException for calDocRef [" + calDocRef
          + "] and nb [" + nb + "] prevOffset [" + prevOffset + "].", exc);
    }
    return prevNavDetails;
  }

  private NavigationDetails getNextNavDetails(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, IEventSearchQuery query,
      EventSearchResult calSearchResult) throws LuceneSearchException {
    NavigationDetails nextNavDetails = navDetails;
    int nextOffset = navDetails.getOffset() + nb;
    LOGGER.debug("getNextNavDetails with query for calDocRef [" + calDocRef + "] and nextOffset ["
        + nextOffset + "] nb [" + nb + "].");
    try {
      if (calSearchResult.getSize() > nextOffset) {
        nextNavDetails = getFirstNavDetails(calDocRef, nextOffset, query, calSearchResult);
      }
    } catch (NavigationDetailException exc) {
      nextNavDetails = null;
      LOGGER.warn("getNextNavDetails encountered NavDetailException for calDocRef [" + calDocRef
          + "] and nb [" + nb + "] nextOffset [" + nextOffset + "].", exc);
    }
    return nextNavDetails;
  }

  private NavigationDetails getFirstNavDetails(DocumentReference calDocRef, int offset,
      IEventSearchQuery query, EventSearchResult searchResult) throws NavigationDetailException,
      LuceneSearchException {
    LOGGER.debug("getFirstNavDetails with query for calDocRef [" + calDocRef + "] and offset ["
        + offset + "].");
    IEvent firstEvent = getFirstElement(searchResult.getEventList(offset, 1));
    return getNavDetailsFactory().getNavigationDetails(calDocRef, firstEvent, query);
  }

  private NavigationDetails getLastNavDetails(DocumentReference calDocRef, int offset,
      IEventSearchQuery query, EventSearchResult searchResult) throws NavigationDetailException,
      LuceneSearchException {
    LOGGER.debug("getLastNavDetails with query for calDocRef [" + calDocRef + "] and offset ["
        + offset + "].");
    IEvent lastEvent = getLastElement(searchResult.getEventList(0, Math.abs(offset)));
    return getNavDetailsFactory().getNavigationDetails(calDocRef, lastEvent, query);
  }

  private UncertainCount[] getCounts(long calSize, long calArchiveSize, int offset, int nb,
      boolean isSearch) {
    LOGGER.info("getCounts: calSize [" + calSize + "], calArchiveSize [" + calArchiveSize
        + "], offset [" + offset + "], nb [" + nb + "], isSearch [" + isSearch + "].");
    boolean[] uncertain = new boolean[3];
    if (isSearch) {
      int resultLimit = getSearchService().getResultLimit(getEventSearchService().skipChecks());
      uncertain[0] = calArchiveSize >= resultLimit;
      uncertain[1] = calSize >= resultLimit;
      uncertain[2] = uncertain[0] || uncertain[1];
    }
    UncertainCount[] counts = new UncertainCount[3];
    counts[0] = new UncertainCount(calArchiveSize + offset, uncertain[0]);
    counts[1] = new UncertainCount(calSize - offset - nb, uncertain[1]);
    counts[2] = new UncertainCount(calSize + calArchiveSize, uncertain[2]);
    return counts;
  }

  private <T> T getFirstElement(List<T> list) throws NavigationDetailException {
    if ((list != null) && (list.size() > 0)) {
      return list.get(0);
    } else if (list == null) {
      LOGGER.warn("getFirstElement called with null-list.");
      throw new NavigationDetailException("getFirstElement called with null-list.");
    } else if (list.isEmpty()) {
      LOGGER.warn("getFirstElement called with empty-list.");
      throw new NavigationDetailException("getFirstElement called with empty-list.");
    }
    throw new IllegalArgumentException("getFirstElement called with illegal list [" + list + "].");
  }

  private <T> T getLastElement(List<T> list) throws NavigationDetailException {
    if ((list != null) && (list.size() > 0)) {
      return list.get(list.size() - 1);
    } else if (list == null) {
      LOGGER.warn("getLastElement called with null-list.");
      throw new NavigationDetailException("getLastElement called with null-list.");
    } else if (list.isEmpty()) {
      LOGGER.warn("getLastElement called with empty-list.");
      throw new NavigationDetailException("getLastElement called with empty-list.");
    }
    throw new IllegalArgumentException("getLastElement called with illegal list [" + list + "].");
  }

  private ICalendar getAllCalendar(ICalendar cal) {
    return getAllCalendar(cal.getDocumentReference());
  }

  private ICalendar getAllCalendar(DocumentReference calDocRef) {
    return getCalService().getCalendar(calDocRef, ICalendarClassConfig.DATE_LOW);
  }

  private ICalendar getAllCalendarReversed(DocumentReference calDocRef) {
    return getCalService().getCalendarArchive(calDocRef, ICalendarClassConfig.DATE_HIGH);
  }

  BenchmarkRole getBenchService() {
    if (benchService == null) {
      benchService = Utils.getComponent(BenchmarkRole.class);
    }
    return benchService;
  }

  ICalendarService getCalService() {
    if (calService == null) {
      calService = Utils.getComponent(ICalendarService.class);
    }
    return calService;
  }

  void injectCalService(ICalendarService calService) {
    this.calService = calService;
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

  private IEventSearchRole getEventSearchService() {
    if (eventSearchService == null) {
      eventSearchService = Utils.getComponent(IEventSearchRole.class);
    }
    return eventSearchService;
  }

  void injectEventSearchService(IEventSearchRole eventSearch) {
    this.eventSearchService = eventSearch;
  }

  private ILuceneSearchService getSearchService() {
    if (searchService == null) {
      searchService = Utils.getComponent(ILuceneSearchService.class);
    }
    return searchService;
  }

  void injectSearchService(ILuceneSearchService searchService) {
    this.searchService = searchService;
  }

}
