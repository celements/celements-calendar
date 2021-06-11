/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.calendar.util;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.service.CalendarService;
import com.celements.calendar.service.ICalendarService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated instead use {@link CalendarService}
 */
@Deprecated
public class CalendarUtils implements ICalendarUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarUtils.class);

  private static ICalendarUtils utilsInstance;

  private ICalendarService calService;

  private CalendarUtils() {}

  public static ICalendarUtils getInstance() {
    if (utilsInstance == null) {
      utilsInstance = new CalendarUtils();
    }
    return utilsInstance;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.celements.calendar.util.ICalendarUtils#getCalendarPageByCalendarSpace(java.lang.String,
   * com.xpn.xwiki.XWikiContext)
   */
  @Override
  @Deprecated
  public XWikiDocument getCalendarPageByCalendarSpace(String calSpace,
      XWikiContext context) throws XWikiException {
    return context.getWiki().getDocument(getCalService().getCalendarDocRefByCalendarSpace(
        calSpace), context);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.calendar.util.ICalendarUtils#getSubscribingCalendars(java.lang.String,
   * com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<String> getSubscribingCalendars(String calSpace,
      XWikiContext context) throws XWikiException {
    List<String> subsCals = Collections.emptyList();
    XWikiDocument cal = getCalendarPageByCalendarSpace(calSpace, context);
    if (cal != null) {
      BaseObject obj = cal.getObject("Classes.CalendarConfigClass");
      if ((obj != null) && (obj.getIntValue("is_subscribable") == 1)) {
        String hql = ", BaseObject as obj, ";
        hql += "DBStringListProperty as str join str.list subto ";
        hql += "where obj.name=doc.fullName ";
        hql += "and obj.className='Classes.CalendarConfigClass' ";
        hql += "and obj.id = str.id.id and str.id.name = 'subscribe_to' ";
        hql += "and subto = '" + cal.getFullName() + "'";
        LOGGER.debug(hql);
        subsCals = context.getWiki().getStore().searchDocumentsNames(hql, context);
      }
    }
    return subsCals;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.celements.calendar.util.ICalendarUtils#getAllowedSpacesHQL(com.xpn.xwiki.doc.XWikiDocument,
   * com.xpn.xwiki.XWikiContext)
   */
  @Override
  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context)
      throws XWikiException {
    return getCalService().getAllowedSpacesHQL(calDoc);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.calendar.util.ICalendarUtils#getCalendar(boolean,
   * com.xpn.xwiki.XWikiContext)
   */
  public ICalendar getCalendar(boolean isArchive, XWikiContext context) {
    return getCalendarByCalDoc(context.getDoc(), isArchive, context);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.calendar.util.ICalendarUtils#getCalendar(java.lang.String, boolean,
   * com.xpn.xwiki.XWikiContext)
   */
  public ICalendar getCalendar(String calDocFullName, boolean isArchive,
      XWikiContext context) {
    try {
      return getCalendarByCalDoc(context.getWiki().getDocument(
          calDocFullName, context), isArchive, context);
    } catch (XWikiException e) {
      LOGGER.error("connot load calender doc", e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.celements.calendar.util.ICalendarUtils#getCalendarByCalDoc(com.xpn.xwiki.doc.XWikiDocument,
   * boolean, com.xpn.xwiki.XWikiContext)
   */
  public ICalendar getCalendarByCalDoc(XWikiDocument calendarDoc,
      boolean isArchive, XWikiContext context) {
    return new Calendar(calendarDoc, isArchive, context);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.calendar.util.ICalendarUtils#getEventSpaceForCalendar(com.xpn.xwiki.doc.
   * XWikiDocument, com.xpn.xwiki.XWikiContext)
   */
  @Override
  @Deprecated
  public String getEventSpaceForCalendar(XWikiDocument doc,
      XWikiContext context) throws XWikiException {
    return getCalService().getEventSpaceForCalendar(doc.getDocumentReference());
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.calendar.util.ICalendarUtils#getEventSpaceForCalendar(java.lang.String,
   * com.xpn.xwiki.XWikiContext)
   */
  @Override
  @Deprecated
  public String getEventSpaceForCalendar(String fullName,
      XWikiContext context) throws XWikiException {
    return getEventSpaceForCalendar(context.getWiki().getDocument(fullName, context),
        context);
  }

  @Override
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef,
      XWikiContext context) throws XWikiException {
    return getEventSpaceForCalendar(context.getWiki().getDocument(calDocRef, context),
        context);
  }

  private ICalendarService getCalService() {
    if (calService == null) {
      calService = Utils.getComponent(ICalendarService.class);
    }
    return calService;
  }

}
