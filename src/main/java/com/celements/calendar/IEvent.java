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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Element;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public interface IEvent {

  public String getTitle(XWikiContext context);

  public String getDescription(XWikiContext context);

  public String getLocation();

  public String getDateString(String dateField, String format);

  public Date getEventDate();

  public Boolean isSubscribable();

  public XWikiDocument getEventDocument();

  public boolean isFromSubscribableCalendar(String calendarSpace);

  @Deprecated
  public String getDocName();

  public DocumentReference getDocumentReference();

  /**
   * Get the value of a String property (String or TextArea). If the value in
   * the desired language is empty, the value of the default language will be
   * returned.
   * 
   * @param name Name of the property
   * @param lang Desired language (if not available default language will be taken).
   * @return 
   */
  public String getStringPropertyDefaultIfEmpty(String name,
      String lang);

  public String getStringProperty(String name, String lang);

  public Date getDateProperty(String name, String lang);

  public Boolean getBooleanProperty(String name, String lang);

  public BaseObject getObj(String lang);
  
  public String displayOverviewField(String name, String link, XWikiContext context);
  
  public String displayField(String name, XWikiContext context);

  public Element[] getProperties(String lang, XWikiContext context);
  
  public List<List<String>> getEditableProperties(String lang, XWikiContext context) throws XWikiException;

  public ICalendar getCalendar(XWikiContext context);

  public List<String> getNonEmptyFields(List<String> fieldList, XWikiContext context);

  public boolean needsMoreLink(XWikiContext context);

}