package com.celements.calendar.util;

import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.service.CalendarService;
import com.celements.calendar.service.ICalendarService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @deprecated instead use {@link CalendarService}
 */
@Deprecated
public interface ICalendarUtils {

  /**
   * @Deprecated instead use {@link ICalendarService#getCalendarDocRefByCalendarSpace}
   */
  @Deprecated
  public XWikiDocument getCalendarPageByCalendarSpace(String calSpace, XWikiContext context)
      throws XWikiException;

  public List<String> getSubscribingCalendars(String calSpace, XWikiContext context)
      throws XWikiException;

  /**
   * @Deprecated instead use {@link ICalendarService#getAllowedSpaces}
   */
  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context)
      throws XWikiException;

  /**
   * @Deprecated instead use {@link ICalendarService#getEventSpaceRefForCalendar}
   */
  @Deprecated
  public String getEventSpaceForCalendar(XWikiDocument doc, XWikiContext context)
      throws XWikiException;

  /**
   * @Deprecated instead use {@link ICalendarService#getEventSpaceRefForCalendar}
   */
  @Deprecated
  public String getEventSpaceForCalendar(String fullName, XWikiContext context)
      throws XWikiException;

  /**
   * @Deprecated instead use {@link ICalendarService#getEventSpaceRefForCalendar}
   */
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context)
      throws XWikiException;

}
