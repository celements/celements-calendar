package com.celements.calendar.service;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.CalendarApi;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.manager.NavigationDetails;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celcalendar")
public class CalendarScriptService implements ScriptService {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CalendarScriptService.class);

  @Requirement
  private ICalendarService calService;

  @Requirement
  private IEventManager eventsMgr;

  @Requirement
  Execution execution;

  @Requirement
  IEventSearch eventSearch;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getEventSpaceForCalendar(DocumentReference calDocRef) {
    try {
      return calService.getEventSpaceForCalendar(calDocRef);
    } catch (XWikiException exp) {
      mLogger.error("failed to getEventSpaceForCalendar [" + calDocRef + "].", exp);
    }
    return null;
  }

  public NavigationDetails getNavigationDetails(CalendarApi cal, EventApi event) {
    try {
      return eventsMgr.getNavigationDetails(new Event(event.getDocumentReference()), 
      		new Calendar(cal.getDocumentReference(), cal.isArchive()));
    } catch (XWikiException e) {
      mLogger.error("Failed to getNavigationDetails.", e);
    }
    return null;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
    ICalendar cal = calService.getCalendarByCalRef(calDocRef, isArchive);
    if (cal != null) {
      return new CalendarApi(cal, getContext());
    } else {
      mLogger.warn("getCalendarByCalRef: failed to get calendar for [" + calDocRef
          + "], [" + isArchive + "].");
    }
    return null;
  }

  public CalendarApi getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive,
      String language) {
    ICalendar cal = calService.getCalendarByCalRef(calDocRef, isArchive);
    if (cal != null) {
      return new CalendarApi(cal, language, getContext());
    } else {
      mLogger.warn("getCalendarByCalRef: failed to get calendar for [" + calDocRef
          + "], [" + isArchive + "], [" + language + "].");
    }
    return null;
  }

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
    return calService.getCalendarDocRefByCalendarSpace(calSpace);
  }
  
  public EventSearchResult getEventSearchResult(LuceneQueryApi query) {
    return eventSearch.getSearchResult(query);
  }
  
  public EventSearchResult getEventSearchResultFromDate(LuceneQueryApi query, 
      Date fromDate) {
    return eventSearch.getSearchResultFromDate(query, fromDate);
  }
  
  public EventSearchResult getEventSearchResultUpToDate(LuceneQueryApi query, 
      Date uptoDate) {
    return eventSearch.getSearchResultUptoDate(query, uptoDate);
  }

}
