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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class CalendarUtils implements ICalendarUtils {
  private static final Log mLogger = LogFactory.getFactory(
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
        mLogger.debug(hql);
        subsCals = context.getWiki().getStore().searchDocumentsNames(hql, context);
      }
    }
    return subsCals;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getAllowedSpacesHQL(com.xpn.xwiki.doc.XWikiDocument, com.xpn.xwiki.XWikiContext)
   */
  @SuppressWarnings("unchecked")
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
      mLogger.error("connot load calender doc", e);
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
   * @see com.celements.calendar.util.ICalendarUtils#getEvents(com.xpn.xwiki.doc.XWikiDocument, int, int, boolean, com.xpn.xwiki.XWikiContext)
   */
  public List<EventApi> getEvents(XWikiDocument calDoc, int start, int nb,
      boolean isArchive, XWikiContext context) throws XWikiException {
    String query = getQuery(calDoc, isArchive, false, context);
    List<EventApi> eventList = new ArrayList<EventApi>();
    try {
      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> eventDocs = storage.search(query, nb, start, context);
      mLogger.debug(eventDocs.size() + " events found. " + eventDocs);
      for (String eventDocName : eventDocs) {
        mLogger.debug(eventDocName);
        if(checkEventSubscription(calDoc, eventDocName, context)){
          eventList.add(new EventApi(new Event(eventDocName, context), context));
        }
      }
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    return eventList;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#countEvents(com.xpn.xwiki.doc.XWikiDocument, boolean, com.xpn.xwiki.XWikiContext)
   */
  public long countEvents(XWikiDocument calDoc, boolean isArchive, XWikiContext context) {
    List<Object> eventCount = null;
    try {
      eventCount = context.getWiki().getStore().search(getQuery(calDoc, isArchive, true,
          context), 0, 0, context);
    } catch (XWikiException e) {
      mLogger.error("Exception while counting number of events for calendar '" + 
          ((calDoc != null)?calDoc.getDocumentReference():calDoc) + "'", e);
    }
    if((eventCount != null) && (eventCount.size() > 0)) {
      mLogger.debug("Count resulted in " + eventCount.get(0) + " which is of class " +
          eventCount.get(0).getClass());
      return (Long)eventCount.get(0);
    }
    return 0;
  }
  
  private boolean checkEventSubscription(XWikiDocument calDoc,
      String eventDocName, XWikiContext context) throws XWikiException {
    boolean isSubscribed = false;
    if(eventDocName.startsWith(getEventSpaceForCalendar(calDoc, context)
        + ".")) {
      isSubscribed = true;
    } else {
      isSubscribed = isEventSubscribed(calDoc, eventDocName, context);
    }
    return isSubscribed;
  }
  
  private boolean isEventSubscribed(XWikiDocument calDoc, String eventDocName,
      XWikiContext context) throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(eventDocName, context);
    BaseObject obj = doc.getObject(CelementsCalendarPlugin.SUBSCRIPTION_CLASS,
        "subscriber", calDoc.getFullName(), false);
    
    XWikiDocument calendarDoc = getCalendarForEvent(doc, context);
    BaseObject calObj = null;
    if(calendarDoc != null){
      calObj = calendarDoc.getObject("Classes.CalendarConfigClass");
    }
    boolean isSubscribed = false;
    if((obj != null) && (obj.getIntValue("doSubscribe") == 1)
        && (calObj != null) && (calObj.getIntValue("is_subscribable") == 1)){
      isSubscribed = true;
    }
    return isSubscribed;
  }
  
  private XWikiDocument getCalendarForEvent(XWikiDocument eventDoc,
      XWikiContext context) throws XWikiException {
    return getCalendarPageByCalendarSpace(eventDoc.getSpace(), context);
  }

  private String getQuery(XWikiDocument calDoc, boolean isArchive, boolean count,
      XWikiContext context) throws XWikiException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    String timeComp = ">=";
    String sortOrder = "asc";
    String selectEmptyDates = "or ec.eventDate is null";
    if(isArchive){
      timeComp = "<";
      sortOrder = "desc";
      selectEmptyDates = "";
    }
    String hql = "select ";
    if(count){
      hql += "count(obj.name)";
    } else {
      hql += "obj.name";
    }
    hql += " from XWikiDocument doc, BaseObject as obj, ";
    hql += CelementsCalendarPlugin.CLASS_EVENT + " as ec ";
    hql += "where doc.fullName = obj.name and doc.translation = 0 and ec.id.id=obj.id ";
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    String defaultLanguage = (String)vcontext.get("default_language");
    hql += "and ec.lang='" + defaultLanguage + "' ";
    hql += "and (ec.eventDate " + timeComp + " '"
      + format.format(getMidnightDate()) + "' " + selectEmptyDates + ") and ";
    hql += getAllowedSpacesHQL(calDoc, context);
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    mLogger.debug(hql);
    
    return hql;
  }
  
  private Date getMidnightDate() {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.set(java.util.Calendar.HOUR, 0);
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    Date dateMidnight = cal.getTime();
    mLogger.debug("date is: " + dateMidnight);
    return dateMidnight;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.util.ICalendarUtils#getEventSpaceForCalendar(com.xpn.xwiki.doc.XWikiDocument, com.xpn.xwiki.XWikiContext)
   */
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
  public String getEventSpaceForCalendar(String fullName,
      XWikiContext context) throws XWikiException {
    return getEventSpaceForCalendar(context.getWiki().getDocument(fullName, context), context);
  }

}
