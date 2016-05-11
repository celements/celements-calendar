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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.calendar.service.ICalendarService;
import com.celements.common.collections.ListUtils;
import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Element;
import com.xpn.xwiki.api.Property;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class Event implements IEvent {

  private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);

  private Map<String, BaseObject> eventObjMap;
  private DocumentReference eventDocRef;
  private String defaultLang;
  private String language;
  private ICalendar calendar;
  private ICalendarService calService;

  /**
   * @deprecated instead use {@link #Event(List)}
   */
  @Deprecated
  public Event(XWikiDocument eventDoc, XWikiContext context) {
    this(eventDoc.getXObjects(new DocumentReference(context.getDatabase(),
        ICalendarClassConfig.CALENDAR_EVENT_CLASS_SPACE,
        ICalendarClassConfig.CALENDAR_EVENT_CLASS_DOC)), eventDoc.getDocumentReference(
        ).getSpaceReferences().get(0).getName(), context);
    this.eventDocRef = eventDoc.getDocumentReference();
  }

  /**
   * @deprecated instead use {@link #Event(List)}
   */
  @Deprecated
  public Event(String eventDocName, XWikiContext context) throws XWikiException {
    this(context.getWiki().getDocument(eventDocName, context), context);
  }

  /**
   * @deprecated instead use {@link #Event(DocumentReference)}
   */
  @Deprecated
  public Event(DocumentReference eventDocRef, XWikiContext context) throws XWikiException {
    this(eventDocRef);
  }

  public Event(DocumentReference eventDocRef) {
    this.eventDocRef = eventDocRef;
  }

  /**
   * @deprecated instead use {@link #Event(List)}
   */
  @Deprecated
  public Event(List<BaseObject> objList, String space, XWikiContext context) {
    this(objList, space);
  }

  /**
   * @deprecated instead use {@link #Event(List)}
   */
  @Deprecated
  public Event(List<BaseObject> objList, String space) {
    this(objList);
  }

  public Event(List<BaseObject> objList) {
    initObjectMap(objList);
    if (getEventObjMap().size() > 0) {
      BaseObject obj = getEventObjMap().values().iterator().next();
      this.eventDocRef = obj.getDocumentReference();
    }
  }

  Map<String, BaseObject> getEventObjMap() {
    if ((eventObjMap == null) && (eventDocRef != null)) {
      try {
        List<BaseObject> objList = getContext().getWiki().getDocument(eventDocRef,
            getContext()).getXObjects(getCalendarEventClassRef());
        initObjectMap(objList);
      } catch (XWikiException exp) {
        LOGGER.error("getEventObjMap failed to get event document [" + eventDocRef + "].",
            exp);
      }
    }
    return eventObjMap;
  }

  private void initObjectMap(List<BaseObject> objList) {
    eventObjMap = new HashMap<String, BaseObject>();
    if (objList != null) {
      for (BaseObject obj : objList) {
        if (obj != null) {
          String lang = obj.getStringValue(ICalendarClassConfig.PROPERTY_LANG);
          if (StringUtils.isNotBlank(lang)) {
            eventObjMap.put(lang, obj);
          } else {
            eventObjMap.put("", obj);
          }
        }
      }
    }
  }

  @Override
  public String getTitle() {
    return getStringPropertyDefaultIfEmpty(ICalendarClassConfig.PROPERTY_TITLE,
        getLanguage());
  }

  @Override
  public String getDescription() {
    return getStringPropertyDefaultIfEmpty(ICalendarClassConfig.PROPERTY_DESCRIPTION,
        getLanguage());
  }

  @Override
  public String getLocation() {
    return getStringProperty(getObj(), ICalendarClassConfig.PROPERTY_LOCATION);
  }

  @Override
  public String getDateString(String dateField, String format) {
    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Event getDateString dateField:[" + 
        dateField + "] format:[" + format + "]");
    return getDateString(dateField, format, null);
  }
  
  @Override
  public String getDateString(String dateField, String format, String language) {
    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Event getDateString dateField:[" + 
        dateField + "] format:[" + format + "] language:[" + language + "]");
    Locale localLanguage = null;
    if((language == null) || (language == "")) {
      localLanguage = new Locale(getLanguage());
    } else {
      localLanguage = new Locale(language);
    }
    SimpleDateFormat sdf = new SimpleDateFormat(format, localLanguage);
    Date date = getDateProperty(getObj(), dateField);
    String dateString = "";
    if (date != null) {
      dateString = sdf.format(date);
    }
    return dateString;
  }

  @Override
  public Date getEventDate() {
    return getDateProperty(getObj(), ICalendarClassConfig.PROPERTY_EVENT_DATE);
  }

  @Override
  public Date getEventEndDate() {
    return getDateProperty(getObj(), ICalendarClassConfig.PROPERTY_EVENT_DATE_END);
  }

  @Override
  public boolean hasTime() {
    return !getCalService().isMidnightDate(getEventDate());
  }

  @Override
  public Boolean isSubscribable() {
    return getBooleanProperty(getObj(),
        ICalendarClassConfig.PROPERTY_EVENT_IS_SUBSCRIBABLE);
  }

  @Override
  public XWikiDocument getEventDocument() {
    try {
      return getContext().getWiki().getDocument(this.eventDocRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get cal doc for [" + this.eventDocRef + "].", exp);
    }
    return null;
  }

  @Deprecated
  @Override
  public String displayOverviewField(String name, String link, XWikiContext context) {
    return displayOverviewField(name, link);
  }

  @Override
  public String displayOverviewField(String name, String link) {
    ICalendar cal = getCalendar(getContext());
    boolean hasLink = cal.hasDetailLink() && needsMoreLink(getContext());
    hasLink &= (!cal.getOverviewFields().contains("detaillink")
        || name.equals("detaillink"));
    LOGGER.debug("cal.hasDetailLink(): " + cal.hasDetailLink());
    LOGGER.debug("needsMoreLink(context): " + needsMoreLink(getContext()));
    LOGGER.debug("!cal.getOverviewFields().contains('detaillink'): "
        + !cal.getOverviewFields().contains("detaillink"));
    LOGGER.debug("name.equals('detaillink'): " + name.equals("detaillink"));
    LOGGER.debug("hasLink: " + hasLink);

    String value = "";
    if (hasLink) {
      value += "<a href='" + link + "'>";
    }
    value += displayField(name, getContext());
    if (hasLink) {
      value += "</a>";
    }

    return value;
  }

  @Deprecated
  @Override
  public String displayField(String name, XWikiContext context) {
    return displayField(name);
  }

  @Override
  public String displayField(String name) {
    return internalDisplayField(name, true);
  }

  String internalDisplayField(String name, boolean addSpans) {
    LOGGER.debug("display: '" + name + "'");
    String value = "";
    String[] parts = name.split("-");
    String prevPartName = null;
    for (String partName : parts) {
      boolean notDisplayIfSame = partName.endsWith(".");
      partName = partName.replaceAll("\\.", "");
      String displayPart = getDisplayPart(partName, notDisplayIfSame);
      if ((parts.length > 1) && !"".equals(displayPart)) {
        displayPart = addDateTimeDelimiter(prevPartName, partName, displayPart);
        if (addSpans) {
          displayPart = "<span class=\"cel_cal_" + partName + "\">" + displayPart
              + "</span>";
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
    if (getContext().get("msg") != null) {
      dictValue = getContext().getMessageTool().get(dictKey);
    } else {
      dictValue = dictKey;
    }
    return dictValue;
  }

  String getDisplayPart(String name, boolean notDisplayIfSame) {
    String value = "";
    if (name.equals("date")) {
      value = getDateString(ICalendarClassConfig.PROPERTY_EVENT_DATE, "dd.MM.yyyy");
    } else if (name.equals("time")) {
      value = getTimeString(ICalendarClassConfig.PROPERTY_EVENT_DATE, notDisplayIfSame);
    } else if (name.equals("date_end")) {
      String startDay = getDateString(ICalendarClassConfig.PROPERTY_EVENT_DATE,
          "dd.MM.yyyy");
      String endDay = getDateString(ICalendarClassConfig.PROPERTY_EVENT_DATE_END,
          "dd.MM.yyyy");
      if (!notDisplayIfSame || !startDay.equals(endDay)) {
        value = endDay;
      }
    } else if (name.equals("time_end")) {
      value = getTimeString(ICalendarClassConfig.PROPERTY_EVENT_DATE_END,
          notDisplayIfSame);
    } else if (name.equals("title")) {
      value = getTitle();
    } else if (name.equals("location")) {
      value = getLocation();
    } else if (name.equals("detaillink")) {
      if (needsMoreLink(getContext())) {
        value = getContext().getMessageTool().get("cel_cal_more_detaillink");
      }
    } else {
      value = getStringProperty(name, getLanguage());
    }
    LOGGER.debug("display part: '" + name + "'" + " = " + value);
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

  @Deprecated
  @Override
  public boolean needsMoreLink(XWikiContext context) {
    return needsMoreLink();
  }

  @Override
  public boolean needsMoreLink() {
    boolean needsLink = false;
    List<String> additionalFields = getAdditionalPropertyNames();
    if (additionalFields != null) {
      needsLink = (getNonEmptyFields(additionalFields).size() > 0);
    }
    return needsLink;
  }

  private List<String> getAdditionalPropertyNames() {
    Set<String> detailFieldsSet = new HashSet<String>();
    detailFieldsSet.addAll(splitIntoPropertyNames(getCalendar().getDetailviewFields()));
    List<String> additionalFields = ListUtils.subtract(
        Arrays.asList(detailFieldsSet.toArray(new String[0])),
        splitIntoPropertyNames(getCalendar().getOverviewFields()));
    return additionalFields;
  }

  @Override
  public boolean isFromSubscribableCalendar(String calendarSpace) {
    boolean result = true;
    if (getDocName().startsWith(calendarSpace + ".")) {
      result = false;
    }
    return result;
  }

  @Deprecated
  @Override
  public String getDocName() {
    for (String key : getEventObjMap().keySet()) {
      return getEventObjMap().get(key).getName();
    }
    return "";
  }

  @Override
  public String getDocumentName() {
    return getDocumentReference().getName();
  }

  @Override
  public DocumentReference getDocumentReference() {
    return this.eventDocRef;
  }

  @Override
  public SpaceReference getEventSpaceRef() {
    return getDocumentReference().getLastSpaceReference();
  }

  @Override
  public String getStringPropertyDefaultIfEmpty(String name, String lang) {
    String result = getStringProperty(getObj(lang), name);
    if ((result.trim().length() <= 0) && !lang.equals(getDefaultLang())) {
      result = getStringProperty(getObj(), name);
    }
    return result;
  }

  @Override
  public String getStringProperty(String name, String lang) {
    return getStringProperty(getObj(lang), name);
  }

  private String getStringProperty(BaseObject obj, String name) {
    String result = "";
    if (obj != null) {
      result = obj.getStringValue(name);
    }
    return result;
  }

  @Override
  public Date getDateProperty(String name, String lang) {
    return getDateProperty(getObj(lang), name);
  }

  private Date getDateProperty(BaseObject obj, String name) {
    Date result = null;
    if (obj != null) {
      result = obj.getDateValue(name);
    }
    return result;
  }

  @Override
  public Boolean getBooleanProperty(String name, String lang) {
    return getBooleanProperty(getObj(lang), name);
  }

  // not set = null, false = 0, true = 1
  private Boolean getBooleanProperty(BaseObject obj, String name) {
    Boolean result = null;
    if (obj != null) {
      int intVal = obj.getIntValue(name, -1);
      if (intVal != -1) {
        result = intVal == 1;
      }
    }
    return result;
  }

  @Override
  public Integer getIntegerProperty(String name, String lang) {
    return getIntegerProperty(getObj(lang), name);
  }

  private Integer getIntegerProperty(BaseObject obj, String name) {
    Integer result = null;
    if (obj != null) {
      int intVal = obj.getIntValue(name, -1);
      if (intVal != -1) {
        result = intVal;
      }
    }
    return result;
  }

  @Override
  public BaseObject getObj() {
    return getObj(getDefaultLang());
  }

  @Override
  public BaseObject getObj(String language) {
    BaseObject obj = null;
    for (String lang : Arrays.asList(language, getDefaultLang(), "")) {
      if ((obj == null) && getEventObjMap().containsKey(lang)) {
        LOGGER.info("getObj: doc '{}', getting object for lang '{}'", getDocumentName(), 
            lang);
        obj = getEventObjMap().get(lang);
      }
    }
    DocumentReference templateDocRef = getTemplateDocRefFromRequest();
    if ((obj == null) && (templateDocRef != null) 
        && getContext().getWiki().exists(templateDocRef, getContext())
        && !getContext().getWiki().exists(getDocumentReference(), getContext())) {
      // new is ok here because the doc doesnt exist and an obj is needed in editor
      obj = new BaseObject();
      obj.setXClassReference(getCalendarEventClassRef());
    }
    return obj;
  }

  private DocumentReference getTemplateDocRefFromRequest() {
    DocumentReference templateDocRef = null;
    if (getContext().getRequest() != null) {
      String template = getContext().getRequest().get("template");
      if (StringUtils.isNotBlank(template)) {
        templateDocRef = getWebUtilsService().resolveDocumentReference(template, 
            getDocumentReference().getWikiReference());
      }
    }
    LOGGER.trace("getTemplateDocRefFromRequest: '{}'", templateDocRef);
    return templateDocRef;
  }

  @Deprecated
  @Override
  public Element[] getProperties(String lang, XWikiContext context) {
    return getProperties(lang);
  }

  @Override
  public Element[] getProperties(String lang) {
    BaseObject obj = getObj(lang);
    if (obj != null) {
      return new com.xpn.xwiki.api.Class(obj.getXClass(getContext()), getContext()
          ).getProperties();
    } else {
      LOGGER.error("getProperties failed. No object found.");
    }
    return new Property[]{};
  }

  @Deprecated
  @Override
  public ICalendar getCalendar(XWikiContext context) {
    return getCalendar();
  }

  @Override
  public ICalendar getCalendar() {
    if (calendar == null) {
      DocumentReference calDocRef = getCalService().getCalendarDocRefByCalendarSpace(
          getEventSpaceRef().getName(), getEventSpaceRef().getParent());
      if (calDocRef != null) {
        calendar = getCalService().getCalendar(calDocRef);
      }
    }
    return calendar;
  }

  @Deprecated
  @Override
  public List<List<String>> getEditableProperties(String lang, XWikiContext context
      ) throws XWikiException {
    return getEditableProperties(lang);
  }

  @Override
  public List<List<String>> getEditableProperties(String lang) throws XWikiException {
    Set<String> confIndep = new HashSet<String>();
    Set<String> confDep = new HashSet<String>();
    splitLanguageDependentFields(confIndep, confDep, splitIntoPropertyNames(
        getCalendar().getOverviewFields()));
    splitLanguageDependentFields(confIndep, confDep, splitIntoPropertyNames(
        getCalendar().getDetailviewFields()));
    if (getCalendar().isSubscribable()) {
      confIndep.add(ICalendarClassConfig.PROPERTY_EVENT_IS_SUBSCRIBABLE);
    }
    Element[] allProps = getProperties(lang);
    LOGGER.debug("getEditableProperties: allProps - " + Arrays.deepToString(allProps));
    LOGGER.debug("getEditableProperties: confIndep - " + Arrays.deepToString(
        confIndep.toArray()));
    LOGGER.debug("getEditableProperties: confDep - " + Arrays.deepToString(
        confDep.toArray()));
    List<String> lIndependantProps = getProps(allProps, confIndep);
    List<String> lDependantProps = getProps(allProps, confDep);
    List<List<String>> editProp = new ArrayList<List<String>>();
    editProp.add(lIndependantProps);
    editProp.add(lDependantProps);
    LOGGER.debug("getEditableProperties: return editProp - "
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

  void splitLanguageDependentFields(Set<String> confIndep, Set<String> confDep,
      List<String> propertyNames) {
    ArrayList<String> propNamesCleanList = new ArrayList<String>();
    propNamesCleanList.addAll(propertyNames);
    propNamesCleanList.remove("detaillink");
    if (propNamesCleanList.contains("date") || propNamesCleanList.contains("time")) {
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
    LOGGER.debug("splitLanguageDepFields: " + propNamesCleanList.toString());
    for (String propName : propNamesCleanList) {
      if (propName.startsWith("l_")) {
        confDep.add(propName);
      } else {
        confIndep.add(propName);
      }
    }
  }

  private List<String> getProps(Element[] allProps, Set<String> conf) {
    List<String> props = new ArrayList<String>();
    for (int i = 0; i < allProps.length; i++) {
      if ((allProps[i] != null) && ((conf.size() == 0) 
          || (conf.contains(allProps[i].getName())))) {
        LOGGER.debug("addProp: " + allProps[i].getName());
        props.add(allProps[i].getName());
      } else {
        LOGGER.debug("NOT addProp: " + allProps[i].getName());
      }
    }
    return props;
  }

  @Deprecated
  @Override
  public List<String> getNonEmptyFields(List<String> fieldList, XWikiContext context) {
    return getNonEmptyFields(fieldList);
  }

  @Override
  public List<String> getNonEmptyFields(List<String> fieldList) {
    List<String> result = new ArrayList<String>();
    for (String fieldName : fieldList) {
      String fieldValue = internalDisplayField(getDetailConfigForField(fieldName), false);
      if ((fieldValue != null) && !getDefaultEmptyDocStrategy().isEmptyRTEString(
          fieldValue)) {
        result.add(fieldName);
      }
    }

    return result;
  }

  String getDetailConfigForField(String fieldName) {
    for (String colFields : getCalendar().getDetailviewFields()) {
      if (isIncludingFieldAsOptional(fieldName, colFields)) {
        fieldName = fieldName + ".";
      }
    }
    return fieldName;
  }

  boolean isIncludingFieldAsOptional(String fieldName, String colFields) {
    return colFields.matches("^([^-]*-)*" + fieldName + "\\.(-[^-]*)*$");
  }

  DocumentReference getCalendarEventClassRef() {
    return getCalClassConf().getCalendarEventClassRef(this.getDocumentReference(
        ).getWikiReference());
  }

  private final String getDefaultLang() {
    if (defaultLang == null) {
      defaultLang = getWebUtilsService().getDefaultLanguage(getEventSpaceRef());
    }
    return defaultLang;
  }

  @Override
  public String getLanguage() {
    if (language != null) {
      return language;
    }
    return getDefaultLang();
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Event) {
      Event other = (Event) obj;
      return new EqualsBuilder().append(this.eventDocRef, other.eventDocRef).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.eventDocRef).toHashCode();
  }

  @Override
  public String toString() {
    return "Event [eventDocRef=" + eventDocRef + ", language=" + language + ", calendar="
        + calendar + ", eventDate=" + getEventDate() + "]";
  }

  protected XWikiContext getContext() {
    Execution execution = Utils.getComponent(Execution.class);
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  /**
   * for Tests only!!!
   */
  void injectEventDoc(XWikiDocument testEventDoc) {
    this.eventDocRef = testEventDoc.getDocumentReference();
  }

  /**
   * for Tests only!!!
   */
  public void injectDocumentReference(DocumentReference eventDocRef) {
    this.eventDocRef = eventDocRef;
  }

  /**
   * for Tests only!!!
   */
  void injectDefaultLanguage(String defLang) {
    defaultLang = defLang;
  }

  /**
   * for Tests only!!!
   */
  void injectCalendar(ICalendar testCalendar) {
    calendar = testCalendar;
  }

  protected ICalendarService getCalService() {
    if (calService == null) {
      calService = Utils.getComponent(ICalendarService.class);
    }
    return calService;
  }

  /**
   * for Tests only!!!
   */
  protected void injectCalService(ICalendarService calService) {
    this.calService = calService;
  }

  protected IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private IDefaultEmptyDocStrategyRole getDefaultEmptyDocStrategy() {
    return Utils.getComponent(IDefaultEmptyDocStrategyRole.class);
  }

  private ICalendarClassConfig getCalClassConf() {
    return Utils.getComponent(ICalendarClassConfig.class);
  }

}
