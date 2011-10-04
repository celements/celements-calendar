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

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.web.XWikiRequest;

public class CelementsCalendarPlugin extends XWikiDefaultPlugin {
  public static final String SUBSCRIPTION_CLASS_SPACE = "Classes";
  public static final String SUBSCRIPTION_CLASS_DOC = "SubscriptionClass";
  public static final String SUBSCRIPTION_CLASS = SUBSCRIPTION_CLASS_SPACE + "." + SUBSCRIPTION_CLASS_DOC;
  
  public static final String CLASS_CALENDAR_SPACE = "Classes";
  public static final String CLASS_CALENDAR_DOC = "CalendarConfigClass";
  public static final String CLASS_CALENDAR = CLASS_CALENDAR_SPACE + "." + CLASS_CALENDAR_DOC;
  public static final String PROPERTY_IS_SUBSCRIBABLE = "is_subscribable";
  public static final String PROPERTY_EVENT_PER_PAGE = "event_per_page";
  public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = "overview_column_config";
  public static final String PROPERTY_EVENT_COLUMN_CONFIG = "event_column_config";
  public static final String PROPERTY_HAS_MORE_LINK = "hasMoreLink";
  public static final String PROPERTY_SUBSCRIBE_TO = "subscribe_to";
  public static final String PROPERTY_CALENDAR_SPACE = "calendarspace";
  
  public static final String CLASS_EVENT_SPACE = "Classes";
  public static final String CLASS_EVENT_DOC = "CalendarEventClass";
  public static final String CLASS_EVENT = CLASS_EVENT_SPACE + "." + CLASS_EVENT_DOC;
  public static final String PROPERTY_LANG = "lang";
  public static final String PROPERTY_TITLE = "l_title";
  public static final String PROPERTY_TITLE_RTE = "l_title_rte";
  public static final String PROPERTY_DESCRIPTION = "l_description";
  public static final String PROPERTY_LOCATION = "location";
  public static final String PROPERTY_LOCATION_RTE = "location_rte";
  public static final String PROPERTY_EVENT_DATE = "eventDate";
  public static final String PROPERTY_EVENT_DATE_END = "eventDate_end";
  public static final String PROPERTY_EVENT_IS_SUBSCRIBABLE = "isSubscribable";
  
  private static final Log mLogger = LogFactory.getFactory().getInstance(CelementsCalendarPlugin.class);
  
  public CelementsCalendarPlugin(String name, String className, XWikiContext context) {
    super(name, className, context);
  }
  
  public String getName() {
    return "celcalendar";
  }
  
  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new CelementsCalendarPluginAPI((CelementsCalendarPlugin) plugin, context);
  }
  
  public void virtualInit(XWikiContext context){
    try{
      getCalendarClass(context);
      getCalendarEventClass(context);
      getSubscriptionClass(context);
    } catch(XWikiException xe){
      //no problem, class can be generated later or manually
      mLogger.error(xe);
    }
  }
  
  private BaseClass getCalendarClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(CLASS_CALENDAR, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(CLASS_CALENDAR_SPACE);
      doc.setName(CLASS_CALENDAR_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(CLASS_CALENDAR);
    needsUpdate |= bclass.addTextField(PROPERTY_CALENDAR_SPACE, PROPERTY_CALENDAR_SPACE, 30);
    String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj, IntegerProperty as int ";
    hql += "where obj.name=doc.fullName ";
    hql += "and not doc.fullName='$doc.getFullName()' ";
    hql += "and obj.className='" + CLASS_CALENDAR + "' ";
    hql += "and int.id.id=obj.id ";
    hql += "and int.id.name='" + PROPERTY_IS_SUBSCRIBABLE + "' ";
    hql += "and int.value='1' ";
    hql += "order by doc.fullName asc";
    needsUpdate |= bclass.addDBListField(PROPERTY_SUBSCRIBE_TO, PROPERTY_SUBSCRIBE_TO, 5, true, hql);
    needsUpdate |= bclass.addTextField(PROPERTY_OVERVIEW_COLUMN_CONFIG, PROPERTY_OVERVIEW_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addTextField(PROPERTY_EVENT_COLUMN_CONFIG, PROPERTY_EVENT_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addNumberField(PROPERTY_EVENT_PER_PAGE, PROPERTY_EVENT_PER_PAGE, 5, "integer");
    needsUpdate |= bclass.addBooleanField(PROPERTY_HAS_MORE_LINK, PROPERTY_HAS_MORE_LINK, "yesno");
    needsUpdate |= bclass.addBooleanField(PROPERTY_IS_SUBSCRIBABLE, PROPERTY_IS_SUBSCRIBABLE, "yesno");
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
  }
  
  private BaseClass getCalendarEventClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(CLASS_EVENT, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(CLASS_EVENT_SPACE);
      doc.setName(CLASS_EVENT_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(CLASS_EVENT);
    needsUpdate |= bclass.addTextField(PROPERTY_LANG, PROPERTY_LANG, 30);
    needsUpdate |= bclass.addTextField(PROPERTY_TITLE, PROPERTY_TITLE, 30);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_TITLE_RTE, PROPERTY_TITLE_RTE, 80, 15);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_DESCRIPTION, PROPERTY_DESCRIPTION, 80, 15);
    needsUpdate |= bclass.addTextField(PROPERTY_LOCATION, PROPERTY_LOCATION, 30);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_LOCATION_RTE, PROPERTY_LOCATION_RTE, 80, 15);
    needsUpdate |= bclass.addDateField(PROPERTY_EVENT_DATE, PROPERTY_EVENT_DATE, null, 0);
    needsUpdate |= bclass.addDateField(PROPERTY_EVENT_DATE_END, PROPERTY_EVENT_DATE_END, null, 0);
    needsUpdate |= bclass.addBooleanField(PROPERTY_EVENT_IS_SUBSCRIBABLE, PROPERTY_EVENT_IS_SUBSCRIBABLE, "yesno");
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
  }
  
  private BaseClass getSubscriptionClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(SUBSCRIPTION_CLASS, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(SUBSCRIPTION_CLASS_SPACE);
      doc.setName(SUBSCRIPTION_CLASS_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(SUBSCRIPTION_CLASS);
    needsUpdate |= bclass.addTextField("subscriber", "subscriber", 30);
    needsUpdate |= bclass.addBooleanField("doSubscribe", "doSubscribe", "yesno");
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
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
