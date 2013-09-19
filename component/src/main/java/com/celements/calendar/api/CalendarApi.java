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

import java.util.Date;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.search.EventSearchQuery;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

public class CalendarApi extends Api {

  private final ICalendar calendar;

  public CalendarApi(ICalendar calendar, XWikiContext context) {
    this(calendar, context.getLanguage(), context);
  }

  public CalendarApi(ICalendar calendar, String language, XWikiContext context) {
    super(context);
    this.calendar = calendar;
    this.calendar.setLanguage(language);
  }

  public List<EventApi> getAllEvents() {
    return EventConverter.getEventApiList(calendar.getAllEventsInternal(),
        calendar.getLanguage(), context);
  }

  public List<EventApi> getEvents(int start, int nb) {
    return EventConverter.getEventApiList(calendar.getEventsInternal(start, nb),
        calendar.getLanguage(), context);
  }

  public EventSearchResultApi searchEvents(EventSearchQuery query) {
    return new EventSearchResultApi(calendar.searchEvents(query), calendar.getLanguage(),
        context);
  }

  public long getNrOfEvents() {
    return calendar.getNrOfEvents();
  }

  public boolean isArchive() {
    return calendar.isArchive();
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

  public DocumentReference getDocumentReference() {
    return calendar.getDocumentReference();
  }

  public void setStartTimestamp(Date newStartDate) {
    calendar.setStartTimestamp(newStartDate);
  }

  public void setStartDate(Date newStartDate) {
    calendar.setStartDate(newStartDate);
  }

  public Date getStartDate() {
    return calendar.getStartDate();
  }

  public EventApi getFirstEvent() {
    return EventConverter.getEventApi(calendar.getFirstEvent(), calendar.getLanguage(),
        context);
  }

  public EventApi getLastEvent() {
    return EventConverter.getEventApi(calendar.getLastEvent(), calendar.getLanguage(),
        context);
  }

}
