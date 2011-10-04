package com.celements.calendar.util;

import java.util.List;

import com.celements.calendar.api.EventApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public interface ICalendarUtils {

  public abstract XWikiDocument getCalendarPageByCalendarSpace(String calSpace,
      XWikiContext context) throws XWikiException;

  public abstract List<String> getSubscribingCalendars(String calSpace,
      XWikiContext context) throws XWikiException;

  public abstract String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context)
      throws XWikiException;

  public abstract List<EventApi> getEvents(XWikiDocument calDoc, int start, int nb,
      boolean isArchive, XWikiContext context) throws XWikiException;

  public abstract long countEvents(XWikiDocument calDoc, boolean isArchive,
      XWikiContext context);

  public abstract String getEventSpaceForCalendar(XWikiDocument doc, XWikiContext context)
      throws XWikiException;

  public abstract String getEventSpaceForCalendar(String fullName, XWikiContext context)
      throws XWikiException;

}