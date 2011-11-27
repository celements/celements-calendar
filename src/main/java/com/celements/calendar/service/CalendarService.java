package com.celements.calendar.service;

import java.util.Date;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class CalendarService implements ICalendarService {

  public static final String CALENDAR_SERVICE_START_DATE =
    "com.celements.calendar.service.CalendarService.startDate";

  @Requirement
  Execution execution;

  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context
      ) throws XWikiException {
    return CalendarUtils.getInstance().getEventSpaceForCalendar(
        context.getWiki().getDocument(calDocRef, context), context);
  }

  public void setStartDate(Date newStartDate) {
    ExecutionContext executionContext = execution.getContext();
    executionContext.setProperty(CALENDAR_SERVICE_START_DATE, newStartDate);
  }

  public Date getStartDate() {
    Date startDate = new Date();
    ExecutionContext executionContext = execution.getContext();
    if (executionContext.getProperty(CALENDAR_SERVICE_START_DATE) != null) {
      startDate = (Date) executionContext.getProperty(CALENDAR_SERVICE_START_DATE);
    }
    return startDate;
  }
  
}
