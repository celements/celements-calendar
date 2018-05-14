package com.celements.calendar.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.CalendarCreateException;
import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.api.CalendarApi;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.navigation.ICalendarNavigationService;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.SearchTermEventSearchQuery;
import com.celements.model.context.ModelContext;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.query.LuceneQuery;

@Component("celcalendar")
public class CalendarScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(CalendarScriptService.class);

  @Requirement
  private IEventManager eventManager;

  @Requirement
  private ICalendarService calService;

  @Requirement
  private ICalendarNavigationService calNavService;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ModelContext context;

  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef) {
    ICalendar cal = createCalendarInternal(calDocRef, false);
    if (cal != null) {
      return cal.getEventSpaceRef();
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

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef, EventApi event) {
    NavigationDetails navDetails = null;
    if (rightsAccess.hasAccessLevel(calConfigDocRef, EAccessLevel.VIEW)) {
      try {
        navDetails = calNavService.getNavigationDetails(calConfigDocRef, eventManager.getEvent(
            event.getDocumentReference()));
      } catch (Exception exc) {
        LOGGER.error("Exception getting navDetails for cal '{}', event '{}'", calConfigDocRef,
            event, exc);
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
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef, Date eventDate,
      int offset, int nb) {
    return getCalendarNavigation(calConfigDocRef, eventDate, offset, nb, false);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef, Date eventDate,
      int offset, int nb, boolean isSendingEmptyPage) {
    CalendarNavigation calNav = null;
    if (rightsAccess.hasAccessLevel(calConfigDocRef, EAccessLevel.VIEW)) {
      try {
        calNav = calNavService.getCalendarNavigation(calConfigDocRef,
            calNavService.getNavigationDetails(eventDate, offset), nb);
      } catch (Exception exc) {
        LOGGER.error("Exception getting calNav for cal '{}', eventDate '{}', "
            + "offset '{}', nb '{}'", calConfigDocRef, eventDate, offset, nb, exc);
      }
    }
    return calNav;
  }

  /**
   * @deprecated instead use {@link #getCalendarNavigation(DocumentReference,
   *             NavigationDetails, int, SearchTermEventSearchQuery, boolean) and specify if a empty
   *             return page is needed or not
   */
  @Deprecated
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef, Date eventDate,
      int offset, int nb, SearchTermEventSearchQuery query) {
    return getCalendarNavigation(calConfigDocRef, eventDate, offset, nb, query, false);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef, Date eventDate,
      int offset, int nb, SearchTermEventSearchQuery query, boolean isSendingEmptyPage) {
    CalendarNavigation calNav = null;
    if (rightsAccess.hasAccessLevel(calConfigDocRef, EAccessLevel.VIEW)) {
      try {
        calNav = calNavService.getCalendarNavigation(calConfigDocRef,
            calNavService.getNavigationDetails(eventDate, offset), nb, query, isSendingEmptyPage);
      } catch (Exception exc) {
        LOGGER.error("Exception getting calNav for cal '{}', eventDate '{}', "
            + "offset '{}', nb '{}', query '{}'", calConfigDocRef, eventDate, offset, nb, query,
            exc);
      }
    }
    return calNav;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
    ICalendar cal = createCalendarInternal(calDocRef, isArchive);
    if (cal != null) {
      return new CalendarApi(cal, context.getXWikiContext());
    }
    return null;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive,
      String language) {
    ICalendar cal = createCalendarInternal(calDocRef, isArchive);
    if (cal != null) {
      return new CalendarApi(cal, language, context.getXWikiContext());
    }
    return null;
  }

  private ICalendar createCalendarInternal(DocumentReference calDocRef, boolean isArchive) {
    ICalendar cal = null;
    if (rightsAccess.hasAccessLevel(calDocRef, EAccessLevel.VIEW)) {
      try {
        if (!isArchive) {
          cal = calService.createCalendar(calDocRef);
        } else {
          cal = calService.createCalendarArchive(calDocRef);
        }
      } catch (CalendarCreateException exc) {
        LOGGER.warn("createCalendar: failed for [{}], [{}]", calDocRef, isArchive, exc);
      }
    }
    return cal;
  }

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
    return calService.getCalendarDocRefByCalendarSpace(calSpace);
  }

  public IEventSearchQuery getEventSearchQuery(LuceneQuery luceneQuery, List<String> sortFields) {
    return getEventSearchQuery(context.getWikiRef(), luceneQuery, sortFields);
  }

  public IEventSearchQuery getEventSearchQuery(WikiReference wikiRef, LuceneQuery luceneQuery,
      List<String> sortFields) {
    return new DefaultEventSearchQuery(wikiRef, luceneQuery.copy(), sortFields);
  }

  public SearchTermEventSearchQuery getSearchTermEventSearchQuery(Date fromDate, Date toDate,
      String searchTerm, List<String> sortFields) {
    return new SearchTermEventSearchQuery(context.getWikiRef(), fromDate, toDate, searchTerm, false,
        sortFields);
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
