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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.search.IEventSearchQuery;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;

public class CalendarApi extends Api {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarApi.class);

  private final ICalendar calendar;

  public CalendarApi(ICalendar calendar, XWikiContext context) {
    this(calendar, context.getLanguage(), context);
  }

  public CalendarApi(ICalendar calendar, String language, XWikiContext context) {
    super(context);
    this.calendar = calendar;
    this.calendar.setLanguage(language);
  }

  public DocumentReference getDocumentReference() {
    return calendar.getDocumentReference();
  }

  public WikiReference getWikiRef() {
    return calendar.getWikiRef();
  }

  public Date getStartDate() {
    return calendar.getStartDate();
  }

  public void setStartTimestamp(Date newStartDate) {
    calendar.setStartTimestamp(newStartDate);
  }

  public void setStartDate(Date newStartDate) {
    calendar.setStartDate(newStartDate);
  }

  public boolean isArchive() {
    return calendar.isArchive();
  }
  
  public SpaceReference getEventSpaceRef() {
    return calendar.getEventSpaceRef();
  }

  public List<EventApi> getAllEvents() {
    try {
      return EventApi.createList(calendar.getAllEventsInternal(), calendar.getLanguage(), 
          context);
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting all events", xwe);
    }
    return Collections.emptyList();
  }

  public List<EventApi> getEvents(int start, int nb) {
    try {
      return EventApi.createList(calendar.getEventsInternal(start, nb), 
          calendar.getLanguage(), context);
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting {} events starting at {}", nb, start, xwe);
    }
    return Collections.emptyList();
  }

  public EventSearchResultApi searchEvents(IEventSearchQuery query) {
    return new EventSearchResultApi(calendar.searchEvents(query), calendar.getLanguage(),
        context);
  }

  public long getNrOfEvents() {
    return calendar.getNrOfEvents();
  }

  public EventApi getFirstEvent() {
    try {
      return EventApi.create(calendar.getFirstEvent(), calendar.getLanguage(), context);
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting first event", xwe);
    }
    return null;
  }

  public EventApi getLastEvent() {
    try {
      return EventApi.create(calendar.getLastEvent(), calendar.getLanguage(), context);
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting last event", xwe);
    }
    return null;
  }

  public List<String> getOverviewFields() {
    return calendar.getOverviewFields();
  }

  public List<String> getDetailviewFields() {
    return calendar.getDetailviewFields();
  }

  public List<String> getCalOverviewPropertyNames() {
    return calendar.getCalOverviewPropertyNames();
  }

  public List<String> getEventPropertyNames() {
    return calendar.getEventPropertyNames();
  }

  public boolean hasDetailLink() {
    return calendar.hasDetailLink();
  }

  public boolean isSubscribable() {
    return calendar.isSubscribable();
  }

}
