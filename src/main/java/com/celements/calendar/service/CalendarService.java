package com.celements.calendar.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CalendarService implements ICalendarService {

  public static final String CALENDAR_SERVICE_START_DATE =
    "com.celements.calendar.service.CalendarService.startDate";

  @Requirement
  Execution execution;

  public String getEventSpaceForCalendar(DocumentReference calDocRef
      ) throws XWikiException {
    return CalendarUtils.getInstance().getEventSpaceForCalendar(
        getContext().getWiki().getDocument(calDocRef, getContext()), getContext());
  }

  public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException {
    return CalendarUtils.getInstance().getAllowedSpacesHQL(calDoc, getContext());
  }

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

}
