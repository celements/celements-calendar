package com.celements.calendar.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.api.CalendarApi;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.manager.NavigationDetails;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celcalendar")
public class CalendarScriptService implements ScriptService {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CalendarScriptService.class);

  @Requirement
  Execution execution;

  @Requirement
  private ICalendarService calService;

  @Requirement
  private IEventManager eventsMgr;

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
      return eventsMgr.getNavigationDetails(new Event(event.getDocumentReference(),
          getContext()), new Calendar(cal.getDocumentReference(), cal.isArchive(),
              getContext()));
    } catch (XWikiException e) {
      mLogger.error("Failed to getNavigationDetails.", e);
    }
    return null;
  }

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

}
