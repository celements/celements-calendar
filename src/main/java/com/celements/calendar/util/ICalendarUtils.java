package com.celements.calendar.util;

import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public interface ICalendarUtils {

  public abstract XWikiDocument getCalendarPageByCalendarSpace(String calSpace,
      XWikiContext context) throws XWikiException;

  public abstract List<String> getSubscribingCalendars(String calSpace,
      XWikiContext context) throws XWikiException;

  public abstract String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context
      ) throws XWikiException;

  public abstract String getEventSpaceForCalendar(XWikiDocument doc, XWikiContext context
      ) throws XWikiException;

  /**
   * 
   * @param fullName
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @Deprecated use service getEventSpaceForCalendar(DocumentReference, XWikiContext)
   *             instead
   */
  @Deprecated
  public abstract String getEventSpaceForCalendar(String fullName, XWikiContext context
      ) throws XWikiException;

  public abstract String getEventSpaceForCalendar(DocumentReference calDocRef,
      XWikiContext context) throws XWikiException;

}