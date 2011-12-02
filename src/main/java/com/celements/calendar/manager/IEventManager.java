package com.celements.calendar.manager;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.EventApi;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IEventManager {

  public List<EventApi> getEvents(ICalendar cal, int start, int nb);

  public long countEvents(XWikiDocument calDoc, boolean isArchive);

  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate);

  public NavigationDetails getNavigationDetails(Event event, Calendar cal
    ) throws XWikiException;

}
