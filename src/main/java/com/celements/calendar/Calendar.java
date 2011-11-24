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
package com.celements.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.EventsManager;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.calendar.util.CalendarUtils;
import com.celements.calendar.util.ICalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class Calendar implements ICalendar {
  private static final String _OVERVIEW_DEFAULT_CONFIG =
    "date,time,l_title,location";

  private static final String _DETAILVIEW_DEFAULT_CONFIG =
    "date,time,l_title,location,l_description";

  private static Log mLogger = LogFactory.getFactory().getInstance(Calendar.class);
  
  private boolean isArchive;
  private XWikiDocument calConfigDoc;
  private static final List<String> _NON_EVENT_PROPERTYS;
  private XWikiContext context;
  private ICalendarUtils utils;

  private IEventManager eventMgr;

  static {
    _NON_EVENT_PROPERTYS = new ArrayList<String>();
    _NON_EVENT_PROPERTYS.add("lang");
    _NON_EVENT_PROPERTYS.add("isSubscribable");
    _NON_EVENT_PROPERTYS.add("eventDate");
    _NON_EVENT_PROPERTYS.add("eventDate_end");
  }
  
  public Calendar(XWikiDocument calConfigDoc, boolean isArchive, XWikiContext context){
    this.context = context;
    this.isArchive = isArchive;
    this.calConfigDoc = calConfigDoc;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getAllEvents()
   */
  public List<EventApi> getAllEvents(){
    return new ArrayList<EventApi>(getEvents(0, 0));
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getEvents(int, int)
   */
  public List<EventApi> getEvents(int start, int nb){
    if(start < 0){ start = 0; }
    if(nb < 0) { nb = 0; }
    List<EventApi> eventList = Collections.emptyList();
    try {
      eventList = getEventMgr().getEvents(calConfigDoc, start, nb, isArchive);
    } catch (XWikiException e) {
      mLogger.error("Exception while getting events for calendar " + calConfigDoc, e);
    }
    return eventList;
  }
  //TODO gesamtnummer und so wie behandeln?
  
  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getNrOfEvents()
   */
  public long getNrOfEvents(){
    return getEventMgr().countEvents(calConfigDoc, isArchive);
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#isArchive()
   */
  public boolean isArchive(){
    return isArchive;
  }

  public List<String> getOverviewFields() {
    return Arrays.asList(getOverviewConfig().split(","));
  }

  public String getOverviewConfig() {
    String overviewConfig = _OVERVIEW_DEFAULT_CONFIG;
    BaseObject calConfigObj = getConfigObject();
    if ((calConfigObj != null)
        && (getPropertyStringValueForOverviewConfig(calConfigObj) != null)
        && (!"".equals(getPropertyStringValueForOverviewConfig(calConfigObj)))) {
      overviewConfig = getPropertyStringValueForOverviewConfig(calConfigObj);
    }
    mLogger.debug("overview config: '" + overviewConfig + "'");
    return overviewConfig;
  }

  private String getPropertyStringValueForOverviewConfig(BaseObject calConfigObj) {
    return calConfigObj.getStringValue(
        CelementsCalendarPlugin.PROPERTY_OVERVIEW_COLUMN_CONFIG);
  }

  public List<String> getDetailviewFields() {
    return Arrays.asList(getDetailviewConfig().split(","));
  }

  public String getDetailviewConfig() {
    String detailviewConfig = _DETAILVIEW_DEFAULT_CONFIG;
    BaseObject calConfigObj = getConfigObject();
    if ((calConfigObj != null)
        && (calConfigObj.getStringValue(CelementsCalendarPlugin.PROPERTY_EVENT_COLUMN_CONFIG) != null)
        && (!"".equals(calConfigObj.getStringValue(CelementsCalendarPlugin.PROPERTY_EVENT_COLUMN_CONFIG)))) {
      detailviewConfig = calConfigObj.getStringValue(CelementsCalendarPlugin.PROPERTY_EVENT_COLUMN_CONFIG);
    }
    mLogger.debug("detailview config: '" + detailviewConfig + "'");
    return detailviewConfig;
  }

  private BaseObject getConfigObject() {
    return calConfigDoc.getXObject(new DocumentReference(context.getDatabase(),
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE,
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC));
  }

  public List<String> getCalOverviewPropertyNames(XWikiContext context) {
    List<String> propNames = getEventPropertyNames(context);
    if (hasDetailLink()) {
      propNames.add("detaillink");
    }
    return propNames;
  }

  public List<String> getEventPropertyNames(XWikiContext context) {
    List<String> propNames = new ArrayList<String>();
    try {
      XWikiDocument doc = context.getWiki().getDocument(new DocumentReference(
          context.getDatabase(), CelementsCalendarPlugin.CLASS_EVENT_SPACE,
          CelementsCalendarPlugin.CLASS_EVENT_DOC), context);
      BaseClass bclass = doc.getXClass();
      Object[] props = bclass.getPropertyNames();
      for(int i = 0; i < props.length; i++) {
        String propertyName = (String) props[i];
        if (!_NON_EVENT_PROPERTYS.contains(propertyName)) {
          propNames.add(propertyName);
        }
      }
      propNames.add("date");
      propNames.add("time");
      propNames.add("date_end");
      propNames.add("time_end");
    } catch (XWikiException e) {
      mLogger.error("Event Class Document not available", e);
    }
    return propNames;
  }

  public boolean hasDetailLink() {
    BaseObject configObj = getConfigObject();
    return ((configObj != null) && (configObj.getIntValue("hasMoreLink") == 1));
  }

  public boolean isSubscribable() {
    BaseObject configObj = getConfigObject();
    return (configObj.getIntValue(
        CelementsCalendarPlugin.PROPERTY_IS_SUBSCRIBABLE) == 1);
  }

  public XWikiDocument getCalDoc() {
    return calConfigDoc;
  }

  public ICalendarUtils getUtils() {
    if(utils == null) {
      utils = CalendarUtils.getInstance();
    }
    return utils;
  }
  
  public void setCalendarUtils(ICalendarUtils utils) {
    this.utils = utils;
  }

  void inject_getEventCmd(IEventManager getEventCmdMock) {
    eventMgr = getEventCmdMock;
  }

  private IEventManager getEventMgr() {
    if (eventMgr == null) {
      eventMgr = (IEventManager) Utils.getComponent(IEventManager.class, "default");
    }
    return eventMgr;
  }

}
