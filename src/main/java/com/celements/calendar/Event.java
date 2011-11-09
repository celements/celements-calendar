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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.calendar.util.CalendarUtils;
import com.celements.common.collections.ListUtils;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Element;
import com.xpn.xwiki.api.Property;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class Event implements IEvent {
  
  public static final String CLASSES_SPACE = "Classes";
  public static final String CALENDAR_EVENT_CLASS_DOC = "CalendarEventClass";
  public static final String CALENDAR_EVENT_CLASS = CLASSES_SPACE + "."
    + CALENDAR_EVENT_CLASS_DOC;

  private static Log mLogger = LogFactory.getFactory().getInstance(Event.class);
  
  private Map<String, BaseObject> eventObj;
  private XWikiDocument eventDoc;
  private String defaultLang;
  private XWikiContext context;

  private ICalendar calendar;
  
  public Event(XWikiDocument eventDoc, XWikiContext context
      ) {
    this(eventDoc.getXObjects(new DocumentReference(context.getDatabase(), CLASSES_SPACE,
        CALENDAR_EVENT_CLASS_DOC)), eventDoc.getDocumentReference().getSpaceReferences(
            ).get(0).getName(), context);
    this.eventDoc = eventDoc;
  }
  
  @Deprecated
  public Event(String eventDocName, XWikiContext context) throws XWikiException{
    this(context.getWiki().getDocument(eventDocName, context), context);
  }
    
  public Event(DocumentReference eventDocRef, XWikiContext context) throws XWikiException{
    this(context.getWiki().getDocument(eventDocRef, context), context);
  }
    
  public Event(List<BaseObject> objList, String space, XWikiContext context) {
    this.context = context;
    if (objList != null) {
      for (BaseObject artObj : objList) {
        init(artObj, space, context);
      }
    }
  }

  Map<String, BaseObject> getEventObjMap() {
    if (eventObj == null) {
      eventObj = new HashMap<String, BaseObject>();
    }
    return eventObj;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#init(BaseObject, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  final private void init(BaseObject artObj, String space,
      XWikiContext context) {
    if (artObj != null) {
      String langValue = artObj.getStringValue(
          CelementsCalendarPlugin.PROPERTY_LANG);
      if (langValue != null) {
        getEventObjMap().put(langValue, artObj);
      } else {
        getEventObjMap().put("", artObj);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getTitle(java.lang.String)
   */
  public String getTitle(XWikiContext context){
    return getStringPropertyDefaultIfEmpty(CelementsCalendarPlugin.PROPERTY_TITLE, context.getLanguage());
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getDescription(java.lang.String)
   */
  public String getDescription(XWikiContext context){
    return getStringPropertyDefaultIfEmpty(CelementsCalendarPlugin.PROPERTY_DESCRIPTION, context.getLanguage());
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getLocation()
   */
  public String getLocation(){
    return getStringProperty(getObj(), CelementsCalendarPlugin.PROPERTY_LOCATION);
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getDateString(java.lang.String)
   */
  public String getDateString(String dateField, String format){
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Date date = getDateProperty(getObj(), dateField);
    String dateString = "";
    if(date != null){
      dateString = sdf.format(date);
    }
    return dateString;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getEventDate()
   */
  public Date getEventDate(){
    return getDateProperty(getObj(), CelementsCalendarPlugin.PROPERTY_EVENT_DATE);
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#isSubscribable()
   */
  public Boolean isSubscribable(){
    return getBooleanProperty(getObj(),
        CelementsCalendarPlugin.PROPERTY_EVENT_IS_SUBSCRIBABLE);
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getEventDocument()
   */
  public XWikiDocument getEventDocument() {
    if ((eventDoc == null) && (getEventObjMap().size() > 0)) {
      BaseObject artObj = getEventObjMap().values().iterator().next();
      try {
        eventDoc = context.getWiki().getDocument(artObj.getDocumentReference(), context);
      } catch (XWikiException e) {
        mLogger.fatal("no EventDocument found", e);
      }
    }
    return eventDoc;
  }
  
  public String displayOverviewField(String name, String link,
      XWikiContext context) {
    ICalendar cal = getCalendar(context);
    boolean hasLink = cal.hasDetailLink() && needsMoreLink(context);
    hasLink &= (!cal.getOverviewFields().contains("detaillink")
        || name.equals("detaillink"));
    mLogger.debug("cal.hasDetailLink(): " + cal.hasDetailLink());
    mLogger.debug("needsMoreLink(context): " + needsMoreLink(context));
    mLogger.debug("!cal.getOverviewFields().contains('detaillink'): "
        + !cal.getOverviewFields().contains("detaillink"));
    mLogger.debug("name.equals('detaillink'): " + name.equals("detaillink"));
    mLogger.debug("hasLink: " + hasLink);
    
    String value = "";
    if(hasLink) { value += "<a href='" + link + "'>"; }
    value += displayField(name, context);
    if(hasLink) { value += "</a>"; }
    
    return value;
  }
  
  public String displayField(String name, XWikiContext context) {
    return internalDisplayField(name, true, context);
  }

  String internalDisplayField(String name, boolean addSpans,
      XWikiContext context) {
    mLogger.debug("display: '" + name + "'");
    String value = "";
    String[] parts = name.split("-");
    String prevPartName = null;
    for(String partName : parts) {
      boolean notDisplayIfSame = partName.endsWith(".");
      partName = partName.replaceAll("\\.", "");
      String displayPart = getDisplayPart(partName, notDisplayIfSame, context);
      if ((parts.length > 1) && !"".equals(displayPart)) {
        displayPart = addDateTimeDelimiter(prevPartName, partName, displayPart);
        if (addSpans) {
          displayPart = "<span class=\"cel_cal_" + partName + "\">"
              + displayPart + "</span>";
        }
      }
      value += displayPart;
      prevPartName = partName;
    }
    return value;
  }

  private String addDateTimeDelimiter(String prevPartName, String partName,
      String displayPart) {
    if (("date_end".equals(partName) || "time_end".equals(partName))
        && ("date".equals(prevPartName) || "time".equals(prevPartName))
        && !"".equals(displayPart)) {
      displayPart = getFromDictionary("cel_cal_datetime_delim") + displayPart;
    }
    return displayPart;
  }

  private String getFromDictionary(String dictKey) {
    String dictValue;
    if (context.get("msg") != null) {
      dictValue = context.getMessageTool().get(dictKey);
    } else {
      dictValue = dictKey;
    }
    return dictValue;
  }

  String getDisplayPart(String name, boolean notDisplayIfSame,
      XWikiContext context) {
    String value = "";
    if(name.equals("date")) {
      value = getDateString(CelementsCalendarPlugin.PROPERTY_EVENT_DATE,
          "dd.MM.yyyy");
    } else if(name.equals("time")) {
      value = getTimeString(CelementsCalendarPlugin.PROPERTY_EVENT_DATE,
          notDisplayIfSame);
    } else if(name.equals("date_end")) {
      String startDay = getDateString(
          CelementsCalendarPlugin.PROPERTY_EVENT_DATE, "dd.MM.yyyy");
      String endDay = getDateString(
          CelementsCalendarPlugin.PROPERTY_EVENT_DATE_END, "dd.MM.yyyy");
      if (!notDisplayIfSame || !startDay.equals(endDay)) {
        value = endDay;
      }
    } else if(name.equals("time_end")) {
      value = getTimeString(CelementsCalendarPlugin.PROPERTY_EVENT_DATE_END,
          notDisplayIfSame);
    } else if(name.equals("title")) {
      value = getTitle(context);
    } else if(name.equals("location")) {
      value = getLocation();
    } else if(name.equals("detaillink")) {
      if(needsMoreLink(context)) {
        value = context.getMessageTool().get("cel_cal_more_detaillink");
      }
    } else {
      value = getStringProperty(name, context.getLanguage());
    }
    mLogger.debug("display part: '" + name + "'" + " = " + value);
    return value;
  }

  private String getTimeString(String fieldName, boolean notDisplayIfSame) {
    String value = "";
    String startTime = getDateString(fieldName, "HH:mm");
    if (!notDisplayIfSame || !startTime.equals("00:00")) {
      value = startTime;
    }
    return value;
  }
  
  public boolean needsMoreLink(XWikiContext context) {
    boolean needsLink = false;
    List<String> additionalFields = getAdditionalPropertyNames(context);
    if(additionalFields != null) {
      needsLink = (getNonEmptyFields(additionalFields, context).size() > 0);
    }
    return needsLink;
  }

  private List<String> getAdditionalPropertyNames(XWikiContext context) {
    Set<String> detailFieldsSet = new HashSet<String>();
    detailFieldsSet.addAll(splitIntoPropertyNames(
        getCalendar(context).getDetailviewFields()));
    List<String> additionalFields = ListUtils.subtract(
        Arrays.asList(detailFieldsSet.toArray(new String[0])),
        splitIntoPropertyNames(getCalendar(context).getOverviewFields()));
    return additionalFields;
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#isFromSubscribableCalendar(java.lang.String)
   */
  public boolean isFromSubscribableCalendar(String calendarSpace){
    boolean result = true;
    if(getDocName().startsWith(calendarSpace + ".")){
      result = false;
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getDocName()
   */
  @Deprecated
  public String getDocName(){
    for (String key : getEventObjMap().keySet()) {
      return getEventObjMap().get(key).getName();
    }
    return "";
  }
  
  public DocumentReference getDocumentReference() {
    if (getEventDocument() != null) {
      return getEventDocument().getDocumentReference();
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getStringPropertyDefaultIfEmpty(java.lang.String, java.lang.String)
   */
  public String getStringPropertyDefaultIfEmpty(String name, String lang){
    String result = getStringProperty(getObj(lang), name);
    if((result.trim().length() <= 0) && !lang.equals(getDefaultLang())){
      result = getStringProperty(getObj(), name);
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getStringProperty(java.lang.String, java.lang.String)
   */
  public String getStringProperty(String name, String lang){
    return getStringProperty(getObj(lang), name);
  }

  private String getStringProperty(BaseObject obj, String name){
    String result = "";
    if(obj != null){
      result = obj.getStringValue(name);
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getDateProperty(java.lang.String, java.lang.String)
   */
  public Date getDateProperty(String name, String lang){
    return getDateProperty(getObj(lang), name);
  }
  
  private Date getDateProperty(BaseObject obj, String name){
    Date result = null;
    if(obj != null){
      result = obj.getDateValue(name);
    }
    return result;
  }
  
  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getBooleanProperty(java.lang.String, java.lang.String)
   */
  public Boolean getBooleanProperty(String name, String lang){
    return getBooleanProperty(getObj(lang), name);
  }
  
  // not set = null, false = 0, true = 1
  private Boolean getBooleanProperty(BaseObject obj, String name){
    Boolean result = null;
    if (obj != null) {
      result = obj.getIntValue(name) == 1;
    }
    return result;
  }
  
  public BaseObject getObj(){
    return getObj(getDefaultLang());
  }

  /* (non-Javadoc)
   * @see com.celements.calendar.IEvent#getObj(java.lang.String)
   */
  public BaseObject getObj(String lang){
    BaseObject obj = null;
    if(getEventObjMap().containsKey(lang)){
      mLogger.info("'" + getDocName() + "' - Getting object for lang '" + lang + "'");
      obj = getEventObjMap().get(lang);
    } else{
      if(getEventObjMap().containsKey(getDefaultLang())){
        mLogger.info("'" + getDocName() + "' - Getting object for defaultLang '" + lang
            + "'");
        obj = getEventObjMap().get(getDefaultLang());
      } else{
        if(getEventObjMap().containsKey("")){
          mLogger.info("'" + getDocName() + "' - Getting object failed for lang ''");
          obj = getEventObjMap().get("");
        } else{
          mLogger.info("'" + getDocName() + "' - Getting object failed for lang '" + lang
              + "' and defaultLang '" + getDefaultLang() + "'");
        }
      }
    }
    mLogger.info("Object found: doc " + (obj != null ? "name='"
        + obj.getDocumentReference() + "'" : "no object found! ") + " obj='" + obj + "'");
    return obj;
  }

  public Element[] getProperties(String lang, XWikiContext context) {
    BaseObject obj = getObj(lang);
    if(obj != null){
      return new com.xpn.xwiki.api.Class(obj.getXClass(context), context).getProperties();
    } else {
      mLogger.error("getProperties failed. No object found.");
    }
    return new Property[]{};
  }
  
  public ICalendar getCalendar(XWikiContext context) {
    if (calendar == null) {
      try {
        XWikiDocument calDoc = CalendarUtils.getInstance(
            ).getCalendarPageByCalendarSpace(getEventPrimarySpace(), context);
        calendar = internal_getCalendarByDoc(calDoc, context);
      } catch (XWikiException e) {
        mLogger.debug("No calendar doc found.", e);
      }
    }
    return calendar;
  }

  public String getEventPrimarySpace() {
    return getDocumentReference().getLastSpaceReference().getName();
  }

  private ICalendar internal_getCalendarByDoc(XWikiDocument calDoc,
      XWikiContext context) {
    if(calDoc != null) {
      return ((CalendarUtils)CalendarUtils.getInstance()).getCalendarByCalDoc(calDoc, 
          false, context);
    }
    return null;
  }
  
  public List<List<String>> getEditableProperties(String lang
      , XWikiContext context) throws XWikiException{
    Set<String> confIndep = new HashSet<String>();
    Set<String> confDep = new HashSet<String>();
    splitLanguageDependentFields(confIndep, confDep,
        splitIntoPropertyNames(getCalendar(context).getOverviewFields()));
    splitLanguageDependentFields(confIndep, confDep,
        splitIntoPropertyNames(getCalendar(context).getDetailviewFields()));
    if (getCalendar(context).isSubscribable()) {
      confIndep.add(CelementsCalendarPlugin.PROPERTY_EVENT_IS_SUBSCRIBABLE);
    }
    Element[] allProps = getProperties(lang, context);
    mLogger.debug("getEditableProperties: allProps - "
        + Arrays.deepToString(allProps));
    mLogger.debug("getEditableProperties: confIndep - "
        + Arrays.deepToString(confIndep.toArray()));
    mLogger.debug("getEditableProperties: confDep - "
        + Arrays.deepToString(confDep.toArray()));
    List<String> lIndependantProps = getProps(allProps, confIndep);
    List<String> lDependantProps= getProps(allProps, confDep);
    List<List<String>> editProp = new ArrayList<List<String>>();
    editProp.add(lIndependantProps);
    editProp.add(lDependantProps);
    mLogger.debug("getEditableProperties: return editProp - "
        + Arrays.deepToString(editProp.toArray()));
    return editProp;
  }

  private List<String> splitIntoPropertyNames(List<String> fieldList) {
    List<String> propertyNames = new ArrayList<String>();
    for (String fieldName : fieldList) {
      if (fieldName.contains("-")) {
        for (String propName : fieldName.split("-")) {
          propertyNames.add(propName.replaceAll("\\.", ""));
        }
      } else {
        propertyNames.add(fieldName.replaceAll("\\.", ""));
      }
    }
    return propertyNames;
  }

  void splitLanguageDependentFields(Set<String> confIndep,
      Set<String> confDep, List<String> propertyNames) {
    ArrayList<String> propNamesCleanList = new ArrayList<String>();
    propNamesCleanList.addAll(propertyNames);
    propNamesCleanList.remove("detaillink");
    if (propNamesCleanList.contains("date")
        || propNamesCleanList.contains("time")) {
      propNamesCleanList.remove("date");
      propNamesCleanList.remove("time");
      propNamesCleanList.add("eventDate");
    }
    if (propNamesCleanList.contains("date_end")
        || propNamesCleanList.contains("time_end")) {
      propNamesCleanList.remove("date_end");
      propNamesCleanList.remove("time_end");
      propNamesCleanList.add("eventDate_end");
    }
    mLogger.debug("splitLanguageDepFields: " + propNamesCleanList.toString());
    for (String propName : propNamesCleanList) {
      if(propName.startsWith("l_")) {
        confDep.add(propName);
      } else {
        confIndep.add(propName);
      }
    }
  }

  private List<String> getProps(Element[] allProps, Set<String> conf) {
    List<String> props = new ArrayList<String>();
    for (int i = 0; i < allProps.length; i++) {
      if((allProps[i] != null) && ((conf.size() == 0)
          || (conf.contains(allProps[i].getName())))){
        mLogger.debug("addProp: " + allProps[i].getName());
        props.add(allProps[i].getName());
      } else {
        mLogger.debug("NOT addProp: " + allProps[i].getName());
      }
    }
    return props;
  }

  public List<String> getNonEmptyFields(List<String> fieldList,
      XWikiContext context) {
    EmptyCheckCommand emptyCheckCmd = new EmptyCheckCommand();
    List<String> result = new ArrayList<String>();
    for (String fieldName : fieldList) {
      String fieldValue = internalDisplayField(
          getDetailConfigForField(fieldName, context), false, context);
      if((fieldValue != null) && !emptyCheckCmd.isEmptyRTEString(fieldValue)) {
        result.add(fieldName);
      }
    }
    
    return result;
  }

  String getDetailConfigForField(String fieldName, XWikiContext context) {
    for(String colFields : getCalendar(context).getDetailviewFields()) {
      if (isIncludingFieldAsOptional(fieldName, colFields)) {
        fieldName = fieldName + ".";
      }
    }
    return fieldName;
  }

  boolean isIncludingFieldAsOptional(String fieldName, String colFields) {
    return colFields.matches("^([^-]*-)*" + fieldName + "\\.(-[^-]*)*$");
  }

  private final String getDefaultLang() {
    if (defaultLang == null) {
      defaultLang = context.getWiki().getWebPreference("default_language",
          getEventPrimarySpace(), "", context);
    }
    return defaultLang;
  }

  /**
   * for Tests only!!!
   * @param xWikiDocument
   */
  void internal_setEventDoc(XWikiDocument testEventDoc) {
    this.eventDoc = testEventDoc;
  }

  /**
   * for Tests only!!!
   * @param string
   */
  void internal_setDefaultLanguage(String defLang) {
    defaultLang = defLang;
  }

  /**
   * for Tests only!!!
   * @param string
   */
  void internal_setCalendar(ICalendar testCalendar) {
    calendar = testCalendar;
  }
}