package com.celements.calendar.util;

import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public interface ICalendarUtils {

  /**
   * 
   * @param calSpace
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @Deprecated instead use calendar service call for
   *             DocumentReference getCalendarDocRefByCalendarSpace(String)
   */
  @Deprecated
  public XWikiDocument getCalendarPageByCalendarSpace(String calSpace,
      XWikiContext context) throws XWikiException;

  public List<String> getSubscribingCalendars(String calSpace, XWikiContext context
      ) throws XWikiException;

  public String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context
      ) throws XWikiException;

  public String getEventSpaceForCalendar(XWikiDocument doc, XWikiContext context
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
  public String getEventSpaceForCalendar(String fullName, XWikiContext context
      ) throws XWikiException;

  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context
      ) throws XWikiException;

}