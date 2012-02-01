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

import com.celements.calendar.api.EventApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public interface ICalendar {

  public List<EventApi> getAllEvents();

  public List<EventApi> getEvents(int start, int nb);

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

  public void setStartDate(Date newStartDate);

  public Date getStartDate();

  public DocumentReference getDocumentReference();

  public String getLanguage();

  public void setLanguage(String language);

}