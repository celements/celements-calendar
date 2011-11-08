package com.celements.calendar.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface ICalendarService {

  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context) throws XWikiException;

}
