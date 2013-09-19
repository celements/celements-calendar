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

  public String getTitle();

  public String getDescription();

  public String getLocation();

  public String getDateString(String dateField, String format);

  public Date getEventDate();

  public Boolean isSubscribable();

  public XWikiDocument getEventDocument();

  public boolean isFromSubscribableCalendar(String calendarSpace);

  @Deprecated
  public String getDocName();

  public String getDocumentName();

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

  /**
   * 
   * @param name
   * @param link
   * @param context
   * @return
   * 
   * @deprecated use instead String displayOverviewField(String, String)
   */
  @Deprecated
  public String displayOverviewField(String name, String link, XWikiContext context);

  public String displayOverviewField(String name, String link);

  /**
   * 
   * @param name
   * @param context
   * @return
   * 
   * @deprecated use instead String displayField(String)
   */
  @Deprecated
  public String displayField(String name, XWikiContext context);

  public String displayField(String name);

  /**
   * 
   * @param lang
   * @param context
   * @return
   * 
   * @deprecated use instead Element[] getProperties(String)
   */
  @Deprecated
  public Element[] getProperties(String lang, XWikiContext context);
  
  public Element[] getProperties(String lang);

  /**
   * 
   * @param lang
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @deprecated use instead List<List<String>> getEditableProperties(String)
   */
  @Deprecated
  public List<List<String>> getEditableProperties(String lang, XWikiContext context
    ) throws XWikiException;

  public List<List<String>> getEditableProperties(String lang) throws XWikiException;

  /**
   * 
   * @param context
   * @return
   * 
   * @deprecated use instead ICalendar getCalendar()
   */
  @Deprecated
  public ICalendar getCalendar(XWikiContext context);

  public ICalendar getCalendar();

  /**
   * 
   * @param fieldList
   * @param context
   * @return
   * 
   * @deprecated use instead List<String> getNonEmptyFields(List<String>)
   */
  @Deprecated
  public List<String> getNonEmptyFields(List<String> fieldList, XWikiContext context);

  public List<String> getNonEmptyFields(List<String> fieldList);

  /**
   * 
   * @param context
   * @return
   * 
   * @deprecated use instead boolean needsMoreLink()
   */
  @Deprecated
  public boolean needsMoreLink(XWikiContext context);

  public boolean needsMoreLink();

  public void setLanguage(String language);

  public String getLanguage();

}