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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CalendarUtils implements ICalendarUtils {

  private static final Log LOGGER = LogFactory.getFactory(
      ).getInstance(CalendarUtils.class);
  
  private static ICalendarUtils utilsInstance;
  
  private CalendarUtils() {}
  
  public static ICalendarUtils getInstance() {
    if (utilsInstance == null) {
      utilsInstance = new CalendarUtils();
    }
    return utilsInstance;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getCalendarPageByCalendarSpace(java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  @Deprecated
  public XWikiDocument getCalendarPageByCalendarSpace(String calSpace,
      XWikiContext context) throws XWikiException{
    String hql = ", BaseObject as obj, StringProperty bspace ";
    hql += "where obj.name=doc.fullName ";
    hql += "and obj.className='Classes.CalendarConfigClass' ";
    hql += "and obj.id = bspace.id.id and bspace.id.name = 'calendarspace' ";
    hql += "and bspace.value = '" + calSpace + "'";
    List<XWikiDocument> blogList = context.getWiki().getStore(
        ).searchDocuments(hql, 1, 0, context);
    if(blogList.size() > 0){
      return blogList.get(0);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getSubscribingCalendars(java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public List<String> getSubscribingCalendars(String calSpace,
      XWikiContext context) throws XWikiException {
    List<String> subsCals = Collections.emptyList();
    XWikiDocument cal = getCalendarPageByCalendarSpace(calSpace, context);
    if (cal != null) {
      BaseObject obj = cal.getObject("Classes.CalendarConfigClass");
      if((obj != null) && (obj.getIntValue("is_subscribable") == 1)) {
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

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getAllowedSpacesHQL(com.xpn.xwiki.doc.XWikiDocument, com.xpn.xwiki.XWikiContext)
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc, XWikiContext context
      ) throws XWikiException {
    // If there is no config object no events should be found
    String spaceHQL = "obj.name like '.%'";
    DocumentReference calConfObjRef = new DocumentReference(context.getDatabase(), 
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE, 
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
    BaseObject calObj = null;
    if(calDoc != null){
      calObj = calDoc.getXObject(calConfObjRef);
    }
    if(calObj != null){
      spaceHQL = "obj.name like '" + calObj.getStringValue(
          CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE) + ".%'";
      
      List subscribedDocNames = calObj.getListValue(
          CelementsCalendarPlugin.PROPERTY_SUBSCRIBE_TO);
      for (Object subDocName : subscribedDocNames) {
        String subDocNameStr = subDocName.toString();
        DocumentReference subDocRef = new DocumentReference(context.getDatabase(), 
            subDocNameStr.split("\\.")[0], subDocNameStr.split("\\.")[1]);
        if(context.getWiki().exists(subDocRef, context)){
          XWikiDocument subscCalDoc = context.getWiki().getDocument(subDocRef, context);
          BaseObject subscCalObj = subscCalDoc.getXObject(calConfObjRef);
          if(subscCalObj != null){
            spaceHQL += " or obj.name like '" + subscCalObj.getStringValue(
                CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE).trim() + ".%'";
          }
        }
      }
      if(spaceHQL.length() > 0){
        spaceHQL = "(" + spaceHQL + ")";
      }
    }
    return spaceHQL;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getCalendar(boolean, com.xpn.xwiki.XWikiContext)
   */
  public ICalendar getCalendar(boolean isArchive, XWikiContext context) {
    return getCalendarByCalDoc(context.getDoc(), isArchive, context);
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getCalendar(java.lang.String, boolean, com.xpn.xwiki.XWikiContext)
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

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getCalendarByCalDoc(com.xpn.xwiki.doc.XWikiDocument, boolean, com.xpn.xwiki.XWikiContext)
   */
  public ICalendar getCalendarByCalDoc(XWikiDocument calendarDoc,
      boolean isArchive, XWikiContext context) {
    return new Calendar(calendarDoc, isArchive, context);
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getEventSpaceForCalendar(com.xpn.xwiki.doc.XWikiDocument, com.xpn.xwiki.XWikiContext)
   */
  @Deprecated
  public String getEventSpaceForCalendar(XWikiDocument doc,
      XWikiContext context) throws XWikiException {
    String space = doc.getName(); // default if no config available
    BaseObject obj = doc.getObject(CelementsCalendarPlugin.CLASS_CALENDAR);
    if(obj != null){
      space = obj.getStringValue(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE);
    }
    return space;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getEventSpaceForCalendar(java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  @Deprecated
  public String getEventSpaceForCalendar(String fullName,
      XWikiContext context) throws XWikiException {
    return getEventSpaceForCalendar(context.getWiki().getDocument(fullName, context),
        context);
  }

  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef,
      XWikiContext context) throws XWikiException {
    return getEventSpaceForCalendar(context.getWiki().getDocument(calDocRef, context),
        context);
  }

}
