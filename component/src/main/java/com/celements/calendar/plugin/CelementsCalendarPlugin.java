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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.calendar.classes.CalendarClasses;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.web.XWikiRequest;

public class CelementsCalendarPlugin extends XWikiDefaultPlugin {

  @Deprecated
  public static final String SUBSCRIPTION_CLASS_SPACE = CalendarClasses.SUBSCRIPTION_CLASS_SPACE;
  @Deprecated
  public static final String SUBSCRIPTION_CLASS_DOC = CalendarClasses.SUBSCRIPTION_CLASS_DOC;
  @Deprecated
  public static final String SUBSCRIPTION_CLASS = CalendarClasses.SUBSCRIPTION_CLASS;

  @Deprecated
  public static final String CLASS_CALENDAR_SPACE = CalendarClasses.CALENDAR_CONFIG_CLASS_SPACE;
  @Deprecated
  public static final String CLASS_CALENDAR_DOC = CalendarClasses.CALENDAR_CONFIG_CLASS_DOC;
  @Deprecated
  public static final String CLASS_CALENDAR = CalendarClasses.CALENDAR_CONFIG_CLASS;
  @Deprecated
  public static final String PROPERTY_IS_SUBSCRIBABLE = CalendarClasses.PROPERTY_IS_SUBSCRIBABLE;
  @Deprecated
  public static final String PROPERTY_EVENT_PER_PAGE = CalendarClasses.PROPERTY_EVENT_PER_PAGE;
  @Deprecated
  public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = CalendarClasses.PROPERTY_OVERVIEW_COLUMN_CONFIG;
  @Deprecated
  public static final String PROPERTY_EVENT_COLUMN_CONFIG = CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG;
  @Deprecated
  public static final String PROPERTY_HAS_MORE_LINK = CalendarClasses.PROPERTY_HAS_MORE_LINK;
  @Deprecated
  public static final String PROPERTY_SUBSCRIBE_TO = CalendarClasses.PROPERTY_SUBSCRIBE_TO;
  @Deprecated
  public static final String PROPERTY_CALENDAR_SPACE = CalendarClasses.PROPERTY_CALENDAR_SPACE;

  @Deprecated
  public static final String CLASS_EVENT_SPACE = CalendarClasses.CALENDAR_EVENT_CLASS_SPACE;
  @Deprecated
  public static final String CLASS_EVENT_DOC = CalendarClasses.CALENDAR_EVENT_CLASS_DOC;
  @Deprecated
  public static final String CLASS_EVENT = CalendarClasses.CALENDAR_EVENT_CLASS;
  @Deprecated
  public static final String PROPERTY_LANG = CalendarClasses.PROPERTY_LANG;
  @Deprecated
  public static final String PROPERTY_TITLE = CalendarClasses.PROPERTY_TITLE;
  @Deprecated
  public static final String PROPERTY_TITLE_RTE = CalendarClasses.PROPERTY_TITLE_RTE;
  @Deprecated
  public static final String PROPERTY_DESCRIPTION = CalendarClasses.PROPERTY_DESCRIPTION;
  @Deprecated
  public static final String PROPERTY_LOCATION = CalendarClasses.PROPERTY_LOCATION;
  @Deprecated
  public static final String PROPERTY_LOCATION_RTE = CalendarClasses.PROPERTY_LOCATION_RTE;
  @Deprecated
  public static final String PROPERTY_EVENT_DATE = CalendarClasses.PROPERTY_EVENT_DATE;
  @Deprecated
  public static final String PROPERTY_EVENT_DATE_END = CalendarClasses.PROPERTY_EVENT_DATE_END;
  @Deprecated
  public static final String PROPERTY_EVENT_IS_SUBSCRIBABLE = CalendarClasses.PROPERTY_EVENT_IS_SUBSCRIBABLE;

  private static final Log mLogger = LogFactory.getFactory().getInstance(CelementsCalendarPlugin.class);

  public CelementsCalendarPlugin(String name, String className, XWikiContext context) {
    super(name, className, context);
  }

  @Override
  public String getName() {
    return "celcalendar";
  }

  @Override
  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new CelementsCalendarPluginAPI((CelementsCalendarPlugin) plugin, context);
  }

  @Override
  public void virtualInit(XWikiContext context){
  }

  /*
   * Form needs:
   * Event document name in eventDoc = Event.Document
   * Language of data in lang = de
   * Language sensitive event details in ed_l_<fieldname> (e.g. ed_l_description="Event Description")
   * Language insensitive detail in ed_n_<fieldname> (e.g. ed_n_location="Hier")
   */
  @SuppressWarnings("unchecked")
  public void saveEvent(XWikiContext context) throws XWikiException{
    XWiki wiki = context.getWiki();
    XWikiRequest request = context.getRequest();
    String docName = request.get("eventDoc");
    XWikiDocument doc = wiki.getDocument(docName, context);
    if(doc.isNew()) { doc.setContent(" "); }
    String lang =  request.get(PROPERTY_LANG);
    BaseObject langobj = doc.getObject(CLASS_EVENT, PROPERTY_LANG, lang, true);
    if(!langobj.getStringValue(PROPERTY_LANG).equals(lang)){
      langobj.setStringValue(PROPERTY_LANG, lang);
      wiki.saveDocument(doc, context);
    }
    List<BaseObject> objs = doc.getObjects(CLASS_EVENT);
    List<String> isSubscribedTo = new ArrayList<String>();
    Map<String, String[]> parameters = request.getParameterMap();
    analyseParameters(parameters, isSubscribedTo, objs, lang, context);

    List<BaseObject> subscriptionObjs = doc.getObjects(SUBSCRIPTION_CLASS);
    changeExistingSubscriptions(subscriptionObjs, isSubscribedTo);
    addNewSubscriptions(doc, isSubscribedTo, context);

    wiki.saveDocument(doc, context);
  }

  private void analyseParameters(Map<String, String[]> parameters,
      List<String> isSubscribedTo, List<BaseObject> objs, String lang,
      XWikiContext context) {
    for (String inputName : parameters.keySet()) {
      if(inputName.startsWith(CelementsCalendarPlugin.CLASS_EVENT + "_") && (inputName.length() >=5)) {
        setParameters(inputName, parameters.get(inputName), lang, objs, context);
      } else if(inputName.startsWith("esub_")){
        String value = "";
        if((parameters.get(inputName) != null) && (parameters.get(inputName).length > 0)) {
          value = parameters.get(inputName)[0];
        }
        isSubscribedTo.add(value);
      }
    }
  }

  private void changeExistingSubscriptions(List<BaseObject> subscriptionObjs,
      List<String> isSubscribedTo) {
    if((subscriptionObjs != null) && (subscriptionObjs.size() > 0)){
      for (BaseObject subsObj : subscriptionObjs) {
        if(subsObj != null){
          String subscriber = subsObj.getStringValue("subscriber");
          if(isSubscribedTo.contains(subscriber)){
            if(subsObj.getIntValue("doSubscribe") != 1){
              subsObj.setIntValue("doSubscribe", 1);
            }
            isSubscribedTo.remove(subscriber);
          } else if(subsObj.getIntValue("doSubscribe") == 1){
            subsObj.setIntValue("doSubscribe", 0);
          }
        }
      }
    }
  }

  private void addNewSubscriptions(XWikiDocument doc,
      List<String> isSubscribedTo, XWikiContext context) throws XWikiException {
    for (String newSubs : isSubscribedTo) {
      BaseObject newObj = doc.newObject(SUBSCRIPTION_CLASS, context);
      newObj.setStringValue("subscriber", newSubs);
      newObj.setIntValue("doSubscribe", 1);
    }
  }

  private void setParameters(String inputName, String[] values,
      String lang, List<BaseObject> objs, XWikiContext context) {
    String value = "";
    if((values != null) && (values.length > 0)) {
      value = values[0];
    }
    String[] inputNameParts = inputName.split("_", 3);
    if (inputNameParts.length == 3) {
      //      String paramClass = inputNameParts[0];
      //      String objNumber = inputNameParts[1];
      String propName = inputNameParts[2];
      mLogger.debug("Save '" + value + "' to '" + propName + "'"
          + " with lang=" + lang);
      for (BaseObject obj : objs) {
        if(needsUpdate(inputName, lang, obj)){
          mLogger.debug(lang + "-Save '" + value + "' to '" + propName + "'");
          obj.set(propName, value, context);
        }
      }
    }
  }

  private boolean needsUpdate(String inputName, String lang, BaseObject obj) {
    return ((obj != null) && (!inputName.startsWith("l_")
        || obj.getStringValue(PROPERTY_LANG).equals(lang)));
  }

}
