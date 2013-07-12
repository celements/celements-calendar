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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.service.ICalendarService;
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

  private static Log LOGGER = LogFactory.getFactory().getInstance(Calendar.class);

  private boolean isArchive;
  private DocumentReference calConfigDocRef;
  private static final List<String> _NON_EVENT_PROPERTYS;

  private IEventManager eventMgr;
  private ICalendarEngineRole engine;
  private ICalendarService calService;

  private Date startDate = new Date();

  private String defaultLang;
  private String language;

  static {
    _NON_EVENT_PROPERTYS = new ArrayList<String>();
    _NON_EVENT_PROPERTYS.add("lang");
    _NON_EVENT_PROPERTYS.add("isSubscribable");
    _NON_EVENT_PROPERTYS.add("eventDate");
    _NON_EVENT_PROPERTYS.add("eventDate_end");
  }

  /**
   * 
   * @param calConfigDoc
   * @param isArchive
   * @param context
   * 
   * @deprecated use Calendar(DocumentReference, boolean) instead
   */
  @Deprecated
  public Calendar(XWikiDocument calConfigDoc, boolean isArchive, XWikiContext context){
    this(calConfigDoc.getDocumentReference(), isArchive);
  }

  /**
   * 
   * @param calConfigDocRef
   * @param isArchive
   * @param context
   * 
   * @deprecated use Calendar(DocumentReference, boolean) instead
   */
  @Deprecated
  public Calendar(DocumentReference calConfigDocRef, boolean isArchive,
      XWikiContext context) {
    this(calConfigDocRef, isArchive);
  }

  public Calendar(DocumentReference calConfigDocRef, boolean isArchive) {
    this.isArchive = isArchive;
    this.calConfigDocRef = calConfigDocRef;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getAllEvents()
   */
  @Deprecated
  public List<EventApi> getAllEvents(){
    return getEventMgr().getEvents(this, 0, 0);
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getAllEvents()
   */
  public List<IEvent> getAllEventsInternal() {
    return getEventMgr().getAllEventsInternal(this);
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getEvents(int, int)
   */
  @Deprecated
  public List<EventApi> getEvents(int start, int nb) {
    return getEventMgr().getEvents(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getEvents(int, int)
   */
  public List<IEvent> getEventsInternal(int start, int nb) {
    return getEventMgr().getEventsInternal(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
  }

  public EventSearchResult searchEvents(EventSearchQuery query) {
    return getEventMgr().searchEvents(this, query);
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.ICalendar#getNrOfEvents()
   */
  public long getNrOfEvents(){
    return getEventMgr().countEvents(this);
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
    LOGGER.debug("overview config: '" + overviewConfig + "'");
    return overviewConfig;
  }

  private String getPropertyStringValueForOverviewConfig(BaseObject calConfigObj) {
    return calConfigObj.getStringValue(
        CalendarClasses.PROPERTY_OVERVIEW_COLUMN_CONFIG);
  }

  public List<String> getDetailviewFields() {
    return Arrays.asList(getDetailviewConfig().split(","));
  }

  public String getDetailviewConfig() {
    String detailviewConfig = _DETAILVIEW_DEFAULT_CONFIG;
    BaseObject calConfigObj = getConfigObject();
    if ((calConfigObj != null)
        && (calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG) != null)
        && (!"".equals(calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG)))) {
      detailviewConfig = calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG);
    }
    LOGGER.debug("detailview config: '" + detailviewConfig + "'");
    return detailviewConfig;
  }

  private BaseObject getConfigObject() {
    return getCalDoc().getXObject(new DocumentReference(getContext().getDatabase(),
        CalendarClasses.CALENDAR_CONFIG_CLASS_SPACE,
        CalendarClasses.CALENDAR_CONFIG_CLASS_DOC));
  }

  public List<String> getCalOverviewPropertyNames() {
    return getCalOverviewPropertyNames_internal(getContext());
  }

  /**
   * @deprecated use getCalOverviewPropertyNames() instead
   */
  @Deprecated
  public List<String> getCalOverviewPropertyNames(XWikiContext context) {
    return getCalOverviewPropertyNames_internal(context);
  }

  private List<String> getCalOverviewPropertyNames_internal(XWikiContext context) {
    List<String> propNames = getEventPropertyNames(context);
    if (hasDetailLink()) {
      propNames.add("detaillink");
    }
    return propNames;
  }

  public List<String> getEventPropertyNames() {
    return getEventPropertyNames_internal(getContext());
  }

  /**
   * @deprecated use getEventPropertyNames() instead
   */
  @Deprecated
  public List<String> getEventPropertyNames(XWikiContext context) {
    return getEventPropertyNames_internal(context);
  }

  private List<String> getEventPropertyNames_internal(XWikiContext context) {
    List<String> propNames = new ArrayList<String>();
    try {
      XWikiDocument doc = context.getWiki().getDocument(new DocumentReference(
          context.getDatabase(), CalendarClasses.CALENDAR_EVENT_CLASS_SPACE,
          CalendarClasses.CALENDAR_EVENT_CLASS_DOC), context);
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
      LOGGER.error("Event Class Document not available", e);
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
        CalendarClasses.PROPERTY_IS_SUBSCRIBABLE) == 1);
  }

  public XWikiDocument getCalDoc() {
    try {
      return getContext().getWiki().getDocument(this.calConfigDocRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get cal doc for [" + this.calConfigDocRef + "].", exp);
    }
    return null;
  }

  public void setStartTimestamp(Date newStartDate) {
    if (newStartDate != null) {
      this.startDate = newStartDate;
    }
  }

  public void setStartDate(Date newStartDate) {
    setStartTimestamp(getCalService().getMidnightDate(newStartDate));
  }

  public Date getStartDate() {
    return this.startDate;
  }

  public DocumentReference getDocumentReference() {
    return calConfigDocRef;
  }

  public String getLanguage() {
    if (this.language != null) {
      return this.language;
    }
    return getDefaultLang();
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  private final String getDefaultLang() {
    if (defaultLang == null) {
      try {
        defaultLang = getContext().getWiki().getSpacePreference("default_language",
            getCalService().getEventSpaceForCalendar(getDocumentReference()), "",
            getContext());
      } catch (XWikiException exp) {
        LOGGER.error("getDefaultLang: failed to get SpacePreferences.", exp);
      }
    }
    return defaultLang;
  }

  public ICalendarEngineRole getEngine() {
    if (engine == null) {
      String engineHint = getContext().getWiki().getXWikiPreference("calendar_engine",
          "calendar.engine", "hql", getContext());
      LOGGER.debug("Using engine '" + engineHint + "' for  calendar '"
          + getDocumentReference() + "'");
      engine = Utils.getComponent(ICalendarEngineRole.class, engineHint);
    }
    return engine;
  }

  public IEvent getFirstEvent() {
    return getEventMgr().getFirstEvent(this);
  }

  public IEvent getLastEvent() {
    return getEventMgr().getLastEvent(this);
  }

  private XWikiContext getContext() {
    Execution execution = Utils.getComponent(Execution.class);
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  void inject_getEventCmd(IEventManager getEventCmdMock) {
    eventMgr = getEventCmdMock;
  }

  private IEventManager getEventMgr() {
    if (eventMgr == null) {
      eventMgr = Utils.getComponent(IEventManager.class, "default");
    }
    return eventMgr;
  }

  private ICalendarService getCalService() {
    if (calService == null) {
      calService = Utils.getComponent(ICalendarService.class);
    }
    return calService;
  }

  @Override
  public String toString() {
    return "Calendar [calConfigDocRef=" + calConfigDocRef + ", startDate=" + startDate
        + ", isArchive=" + isArchive + ", engine=" + getEngine() + ", language="
        + getLanguage() + ", defaultLang=" + getDefaultLang() + "]";
  }

}
