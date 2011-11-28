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
package com.celements.calendar.plugin;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.CalendarApi;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

public class CelementsCalendarPluginAPI extends Api {

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      CelementsCalendarPluginAPI.class);

  private CelementsCalendarPlugin calPlugin;
  
  public CelementsCalendarPluginAPI(CelementsCalendarPlugin plugin, XWikiContext context) {
    super(context);
    this.calPlugin = plugin;
  }
  
  public CelementsCalendarPlugin getPlugin() {
    return calPlugin;
  }
  
  public void setPlugin(CelementsCalendarPlugin plugin) {
    this.calPlugin = plugin;
  }
  
  public ICalendar getCalendar(){
    return ((CalendarUtils)CalendarUtils.getInstance()).getCalendar(false, context);
  }
  
  public CalendarApi getCalendar(boolean isArchive){
    return new CalendarApi(((CalendarUtils)CalendarUtils.getInstance()).getCalendar(
        isArchive, context), context);
  }
  
  public CalendarApi getCalendar(String calDocFullName, boolean isArchive){
    return new CalendarApi(((CalendarUtils)CalendarUtils.getInstance()).getCalendar(
        calDocFullName, isArchive, context), context);
  }
  
  public CalendarApi getCalendarArchive(){
    return new CalendarApi(((CalendarUtils)CalendarUtils.getInstance()).getCalendar(true, 
        context), context);
  }
  
  /**
   * @deprecated instead use getEvent(DocumentReference)
   */
  @Deprecated
  public EventApi getEvent(String docName) throws XWikiException {
    return new EventApi(new Event(docName, context), context);
  }
  
  public EventApi getEvent(DocumentReference eventDocRef) throws XWikiException {
    return new EventApi(new Event(eventDocRef, context), context);
  }
  
  public void saveEvent() throws XWikiException{
    calPlugin.saveEvent(context);
  }
  
  public Document getCalendarPageByCalendarSpace(String calSpace) throws XWikiException{
    Document calDoc = null;
    XWikiDocument xCalDoc = CalendarUtils.getInstance().getCalendarPageByCalendarSpace(calSpace, context);
    if(xCalDoc != null){
      calDoc = xCalDoc.newDocument(context);
    }
    return calDoc;
  }

  /**
   * @Deprecated use service getEventSpaceForCalendar(DocumentReference) instead
   */
  @Deprecated
  public String getEventSpaceForCalendar(String fullName) throws XWikiException{
    mLogger.warn("Deprecated method getEventSpaceForCalendar used: "
        + context.getDoc().getFullName() + " for calendar [" + fullName + "].");
    return CalendarUtils.getInstance().getEventSpaceForCalendar(fullName, context);
  }
  
  public String getEventSpaceForCalendar(DocumentReference calDocRef) throws XWikiException{
    return CalendarUtils.getInstance().getEventSpaceForCalendar(calDocRef, context);
  }
  
  public List<String> getSubscribingCalendars(String calEventSpace) throws XWikiException{
    return CalendarUtils.getInstance().getSubscribingCalendars(calEventSpace, context);
  }

}
