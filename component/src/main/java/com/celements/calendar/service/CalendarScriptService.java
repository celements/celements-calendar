package com.celements.calendar.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.api.CalendarApi;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.navigation.ICalendarNavigationService;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.IDateEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.SearchTermEventSearchQuery;
import com.celements.performance.BenchmarkRole;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celcalendar")
public class CalendarScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarScriptService.class);

  @Requirement
  private IEventManager eventManager;

  @Requirement
  private ICalendarService calService;

  @Requirement
  private ICalendarNavigationService calNavService;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  @Requirement
  private BenchmarkRole benchSrv;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef) {
    try {
      return calService.getEventSpaceRefForCalendar(calDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("failed to getEventSpaceRefForCalendar [" + calDocRef + "].", exp);
    }
    return null;
  }

  /**
   * @deprecated instead use {@link #getEventSpaceRefForCalendar}
   */
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef) {
    return getEventSpaceRefForCalendar(calDocRef).getName();
  }

  public NavigationDetails getNavigationDetails(CalendarApi cal, EventApi event) {
    return getNavigationDetails(cal.getDocumentReference(), event);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      EventApi event) {
    NavigationDetails navDetails = null;
    if (hasViewRights(calConfigDocRef)) {
      try {
        navDetails = calNavService.getNavigationDetails(calConfigDocRef,
            eventManager.getEvent(event.getDocumentReference()));
      } catch (Exception exc) {
        LOGGER.error("Exception getting navDetails for cal '{}', event '{}'",
            calConfigDocRef, event, exc);
      }
    }
    return navDetails;
  }

  /**
   * @deprecated instead use {@link #getCalendarNavigation(DocumentReference,
   *             NavigationDetails, int, boolean) and specify if a empty
   *             return page is needed or not
   */
  @Deprecated
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb) {
    return getCalendarNavigation(calConfigDocRef, eventDate, offset, nb, false);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb, boolean isSendingEmptyPage) {
    CalendarNavigation calNav = null;
    if (hasViewRights(calConfigDocRef)) {
      try {
        NavigationDetails navigationDetails = calNavService.getNavigationDetails(eventDate, offset);
        benchSrv.bench("getCalendarNavigation after getNavigationDetails");
        calNav = calNavService.getCalendarNavigation(calConfigDocRef, navigationDetails, nb);
        benchSrv.bench("getCalendarNavigation after getCalendarNavigation");
      } catch (Exception exc) {
        LOGGER.error("Exception getting calNav for cal '{}', eventDate '{}', "
            + "offset '{}', nb '{}'", calConfigDocRef, eventDate, offset, nb, exc);
      }
    }
    return calNav;
  }

  /**
   * @deprecated instead use {@link #getCalendarNavigation(DocumentReference,
   *             NavigationDetails, int, IDateEventSearchQuery, boolean) and specify if a empty
   *             return page is needed or not
   */
  @Deprecated
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb, SearchTermEventSearchQuery query) {
    return getCalendarNavigation(calConfigDocRef, eventDate, offset, nb, query, false);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb, IDateEventSearchQuery query, boolean isSendingEmptyPage) {
    CalendarNavigation calNav = null;
    if (hasViewRights(calConfigDocRef)) {
      try {
        calNav = calNavService.getCalendarNavigation(calConfigDocRef,
            calNavService.getNavigationDetails(eventDate, offset), nb, query,
            isSendingEmptyPage);
      } catch (Exception exc) {
        LOGGER.error("Exception getting calNav for cal '{}', eventDate '{}', "
            + "offset '{}', nb '{}', query '{}'", calConfigDocRef, eventDate, offset, nb,
            query, exc);
      }
    }
    return calNav;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
    ICalendar cal;
    if (!isArchive) {
      cal = calService.getCalendar(calDocRef);
    } else {
      cal = calService.getCalendarArchive(calDocRef);
    }
    if (cal != null) {
      return new CalendarApi(cal, getContext());
    } else {
      LOGGER.warn("getCalendarByCalRef: failed to get calendar for [{}], [{}].", calDocRef,
          isArchive);
    }
    return null;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive,
      String language) {
    ICalendar cal;
    if (!isArchive) {
      cal = calService.getCalendar(calDocRef);
    } else {
      cal = calService.getCalendarArchive(calDocRef);
    }
    if (cal != null) {
      return new CalendarApi(cal, language, getContext());
    } else {
      LOGGER.warn("getCalendarByCalRef: failed to get calendar for [{}], [{}], [{}].", calDocRef,
          isArchive, language);
    }
    return null;
  }

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
    return calService.getCalendarDocRefByCalendarSpace(calSpace);
  }

  public IEventSearchQuery getEventSearchQuery(LuceneQuery luceneQuery,
      List<String> sortFields) {
    return getEventSearchQuery(webUtilsService.getWikiRef(), luceneQuery, sortFields);
  }

  public IEventSearchQuery getEventSearchQuery(WikiReference wikiRef,
      LuceneQuery luceneQuery, List<String> sortFields) {
    return new DefaultEventSearchQuery(wikiRef, luceneQuery.copy(),
        sortFields);
  }

  public SearchTermEventSearchQuery getSearchTermEventSearchQuery(Date fromDate,
      Date toDate, String searchTerm, List<String> sortFields) {
    return new SearchTermEventSearchQuery(webUtilsService.getWikiRef(), fromDate, toDate,
        searchTerm, false, sortFields);
  }

  private boolean hasViewRights(DocumentReference docRef) {
    boolean ret = false;
    try {
      String fullName = webUtilsService.getRefDefaultSerializer().serialize(docRef);
      ret = getContext().getWiki().getRightService().hasAccessLevel("view", getContext().getUser(),
          fullName, getContext());
    } catch (XWikiException exc) {
      LOGGER.error("Failed to check rights for docRef [{}]", docRef, exc);
    }
    LOGGER.debug("hasViewRights for docRef [{}] returned [{}]", docRef, ret);
    return ret;
  }

  public boolean isMidnightDate(Date date) {
    return calService.isMidnightDate(date);
  }

  public Date getMidnightDate(Date date) {
    return calService.getMidnightDate(date);
  }

  public Date getEndOfDayDate(Date date) {
    return calService.getEndOfDayDate(date);
  }

  public Date getHighDate() {
    return ICalendarClassConfig.DATE_HIGH;
  }

  public Date getLowDate() {
    return ICalendarClassConfig.DATE_LOW;
  }

}
