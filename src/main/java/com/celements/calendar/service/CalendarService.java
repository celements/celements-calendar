package com.celements.calendar.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class CalendarService implements ICalendarService {

  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context
      ) throws XWikiException {
    return CalendarUtils.getInstance().getEventSpaceForCalendar(
        context.getWiki().getDocument(calDocRef, context), context);
  }

}
