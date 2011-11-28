package com.celements.calendar.service;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiException;

@Component("celcalendar")
public class CalendarScriptService implements ScriptService {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CalendarScriptService.class);

  @Requirement
  Execution execution;

  @Requirement
  private ICalendarService calService;

  public String getEventSpaceForCalendar(DocumentReference calDocRef) {
    try {
      return calService.getEventSpaceForCalendar(calDocRef);
    } catch (XWikiException exp) {
      mLogger.error("failed to getEventSpaceForCalendar [" + calDocRef + "].", exp);
    }
    return null;
  }

  public void setStartDate(Date newStartDate) {
    calService.setStartDate(newStartDate);
  }

  public Date getStartDate() {
    return calService.getStartDate();
  }

}
