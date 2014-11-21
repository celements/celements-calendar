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

import java.util.Date;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public interface ICalendar {

  /**
   * getAllEvents
   * 
   * @return
   * 
   * @deprecated instead use getAllEvents from CalendarApi or getAllEventsInternal
   *             getAllEvents returning EventsApi will be moved to CalendarApi class.
   */
  @Deprecated
  public List<EventApi> getAllEvents();

  public List<IEvent> getAllEventsInternal();

  /**
   * getEvents
   * 
   * @return
   * 
   * @deprecated instead use getAllEvents from CalendarApi or getEventsInternal
   *             getEvents returning EventsApi will be moved to CalendarApi class.
   */
  @Deprecated
  public List<EventApi> getEvents(int start, int nb);

  /**
   * use internal only. Do not return IEvent objects to the velocity
   * 
   * @param start
   * @param nb
   * @return
   */
  public List<IEvent> getEventsInternal(int start, int nb);

  public EventSearchResult searchEvents(IEventSearchQuery query);

  public long getNrOfEvents();

  public boolean isArchive();

  public List<String> getOverviewFields();

  public List<String> getDetailviewFields();

  public List<String> getCalOverviewPropertyNames();

  /**
   * @deprecated use getCalOverviewPropertyNames() instead
   */
  @Deprecated
  public List<String> getCalOverviewPropertyNames(XWikiContext context);

  public List<String> getEventPropertyNames();

  /**
   * @deprecated use getEventPropertyNames() instead
   */
  @Deprecated
  public List<String> getEventPropertyNames(XWikiContext context);

  public boolean hasDetailLink();

  public boolean isSubscribable();

  public XWikiDocument getCalDoc();

  /**
   * Sets date and time of Calendar
   * 
   * @param newStartDate
   */
  public void setStartTimestamp(Date newStartDate);

  /**
   * Sets only date of Calendar (time to midnight)
   * 
   * @param newStartDate
   */
  public void setStartDate(Date newStartDate);

  public Date getStartDate();

  public DocumentReference getDocumentReference();
  
  public SpaceReference getEventSpaceRef();

  public String getLanguage();

  public void setLanguage(String language);
  
  public List<String> getAllowedSpaces();

  public ICalendarEngineRole getEngine();

  public IEvent getFirstEvent();

  public IEvent getLastEvent();

}