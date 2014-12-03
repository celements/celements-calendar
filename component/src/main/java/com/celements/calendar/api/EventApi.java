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
package com.celements.calendar.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Element;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class EventApi extends Api {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventApi.class);

  private static IWebUtilsService webUtilsService;

  private final IEvent event;

  public EventApi(IEvent event, XWikiContext context) {
    this(event, context.getLanguage(), context);
  }

  public EventApi(IEvent event, String language, XWikiContext context) {
    super(context);
    this.event = event;
    this.event.setLanguage(language);
  }

  public CalendarApi getCalendar() {
    ICalendar calendar = event.getCalendar();
    if (calendar != null) {
      return new CalendarApi(calendar, context);
    }
    return null;
  }

  public String getTitle() {
    return event.getTitle();
  }

  public String getDescription() {
    return event.getDescription();
  }

  public String getLocation() {
    return event.getLocation();
  }

  public String getDateString(String dateField, String format) {
    return event.getDateString(dateField, format);
  }

  public Date getEventDate() {
    return event.getEventDate();
  }
  
  public Date getEventEndDate() {
    return event.getEventEndDate();
  }

  public boolean hasTime() {
    return event.hasTime();
  }

  public Boolean isSubscribable() {
    return event.isSubscribable();
  }

  public Document getEventDocument() {
    return event.getEventDocument().newDocument(context);
  }

  /**
   * 
   * @return
   * 
   * @deprecated use getDocumentReference instead
   */
  @Deprecated
  public String getDocName() {
    return event.getDocName();
  }

  public DocumentReference getDocumentReference() {
    return event.getDocumentReference();
  }

  public boolean isFromSubscribableCalendar(String calendarSpace) {
    return event.isFromSubscribableCalendar(calendarSpace);
  }

  /**
   * Get the value of a String property (String or TextArea). If the value in
   * the desired language is empty, the value of the default language will be
   * returned.
   * 
   * @param name Name of the property
   * @param lang Desired language (if not available default language will be taken).
   * @return
   */
  public String getStringPropertyDefaultIfEmpty(String name){
    return event.getStringPropertyDefaultIfEmpty(name, context.getLanguage());
  }

  public String getStringProperty(String name) {
    return event.getStringProperty(name, context.getLanguage());
  }

  public Date getDateProperty(String name) {
    return event.getDateProperty(name, context.getLanguage());
  }

  public Boolean getBooleanProperty(String name, String lang) {
    return event.getBooleanProperty(name, context.getLanguage());
  }
  
  public Integer getIntegerProperty(String name) {
    return event.getIntegerProperty(name, context.getLanguage());
  }

  public com.xpn.xwiki.api.Object getObj(String lang) {
    BaseObject obj = event.getObj(lang);
    if (obj != null) {
      return obj.newObjectApi(obj, context);
    }
    return null;
  }

  public Element[] getProperties() {
    return event.getProperties(context.getLanguage());
  }

  public String displayOverviewField(String name, String link) {
    return event.displayOverviewField(name, link);
  }

  public String displayField(String name) {
    return event.displayField(name);
  }

  public List<List<String>> getEditableProperties() throws XWikiException{
    return event.getEditableProperties(context.getLanguage());
  }

  public List<String> getNonEmptyFields(List<String> fieldList) {
    return event.getNonEmptyFields(fieldList);
  }

  public boolean needsMoreLink(){
    return event.needsMoreLink();
  }

  public String getLanguage() {
    return event.getLanguage();
  }

  public void setLanguage(String language) {
    event.setLanguage(language);
  }

  @Override
  public String toString() {
    return event.toString();
  }

  public static List<EventApi> createList(List<IEvent> eventList, XWikiContext context
      ) throws XWikiException {
    return createList(eventList, context.getLanguage(), context);
  }

  public static List<EventApi> createList(List<IEvent> eventList, String language,
      XWikiContext context) throws XWikiException {
    long time = System.currentTimeMillis();
    List<EventApi> eventApiList = new ArrayList<EventApi>();
    for (IEvent event : eventList) {
      EventApi eventApi = create(event, language, context);
      if (eventApi != null) {
        eventApiList.add(create(event, language, context));
      }
    }
    time = System.currentTimeMillis() - time;
    LOGGER.debug("createList: for {} elements took {}ms", eventList.size(), time);
    return eventApiList;
  }

  public static EventApi create(IEvent event, XWikiContext context) throws XWikiException {
    return create(event, context.getLanguage(), context);
  }

  public static EventApi create(IEvent event, String language, XWikiContext context
      ) throws XWikiException {
    EventApi eventApi = null;
    if (event != null) {
      String fullName = getWebUtilsService().getRefDefaultSerializer().serialize(
          event.getDocumentReference());
      if (context.getWiki().getRightService().hasAccessLevel("view", context.getUser(), 
          fullName, context)) {
        eventApi = new EventApi(event, language, context);
      } else {
        LOGGER.debug("No view rights on '{}' for user '{}'", fullName, context.getUser());
      }
    }
    return eventApi;
  }

  private static IWebUtilsService getWebUtilsService() {
    if (webUtilsService == null) {
      webUtilsService = Utils.getComponent(IWebUtilsService.class);
    }
    return webUtilsService;
  }

}