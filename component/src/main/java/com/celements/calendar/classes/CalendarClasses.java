package com.celements.calendar.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.common.classes.AbstractClassCollection;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.CalendarClasses")
public class CalendarClasses extends AbstractClassCollection {

  @Deprecated
  public static final String CALENDAR_CONFIG_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String CALENDAR_CONFIG_CLASS_DOC = "CalendarConfigClass";
  @Deprecated
  public static final String CALENDAR_CONFIG_CLASS = CALENDAR_CONFIG_CLASS_SPACE + "."
      + CALENDAR_CONFIG_CLASS_DOC;
  @Deprecated
  public static final String PROPERTY_IS_SUBSCRIBABLE = "is_subscribable";
  @Deprecated
  public static final String PROPERTY_EVENT_PER_PAGE = "event_per_page";
  @Deprecated
  public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = "overview_column_config";
  @Deprecated
  public static final String PROPERTY_EVENT_COLUMN_CONFIG = "event_column_config";
  @Deprecated
  public static final String PROPERTY_HAS_MORE_LINK = "hasMoreLink";
  @Deprecated
  public static final String PROPERTY_SUBSCRIBE_TO = "subscribe_to";
  @Deprecated
  public static final String PROPERTY_CALENDAR_SPACE = "calendarspace";

  @Deprecated
  public static final String CALENDAR_EVENT_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String CALENDAR_EVENT_CLASS_DOC = "CalendarEventClass";
  @Deprecated
  public static final String CALENDAR_EVENT_CLASS = CALENDAR_EVENT_CLASS_SPACE + "."
      + CALENDAR_EVENT_CLASS_DOC;
  @Deprecated
  public static final String PROPERTY_LANG = "lang";
  @Deprecated
  public static final String PROPERTY_TITLE = "l_title";
  @Deprecated
  public static final String PROPERTY_TITLE_RTE = "l_title_rte";
  @Deprecated
  public static final String PROPERTY_DESCRIPTION = "l_description";
  @Deprecated
  public static final String PROPERTY_LOCATION = "location";
  @Deprecated
  public static final String PROPERTY_LOCATION_RTE = "location_rte";
  @Deprecated
  public static final String PROPERTY_EVENT_DATE = "eventDate";
  @Deprecated
  public static final String PROPERTY_EVENT_DATE_END = "eventDate_end";
  @Deprecated
  public static final String PROPERTY_EVENT_DATE_VALIDATION = "cel_calendar_validation_event_date";
  @Deprecated
  public static final String PROPERTY_EVENT_DATE_END_VALIDATION = "cel_calendar_validation_event_end_date";
  @Deprecated
  public static final String PROPERTY_EVENT_DATE_FORMAT = "dd.MM.yyyy HH:mm";
  @Deprecated
  public static final String PROPERTY_EVENT_IS_SUBSCRIBABLE = "isSubscribable";

  @Deprecated
  public static final String SUBSCRIPTION_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String SUBSCRIPTION_CLASS_DOC = "SubscriptionClass";
  @Deprecated
  public static final String SUBSCRIPTION_CLASS = SUBSCRIPTION_CLASS_SPACE + "."
      + SUBSCRIPTION_CLASS_DOC;
  @Deprecated
  public static final String PROPERTY_SUBSCRIBER = "subscriber";
  @Deprecated
  public static final String PROPERTY_DO_SUBSCRIBE = "doSubscribe";

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarClasses.class);

  @Requirement
  private ICalendarClassConfig classConf;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  protected void initClasses() throws XWikiException {
    getCalendarClass();
    getCalendarEventClass();
    getSubscriptionClass();
  }

  @Override
  public String getConfigName() {
    return "celCalendar";
  }

  private BaseClass getCalendarClass() throws XWikiException {
    DocumentReference classRef = getCalendarClassRef(getContext().getDatabase());
    XWikiDocument doc;
    boolean needsUpdate = false;

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (Exception exception) {
      LOGGER.error("Exception while getting doc for ClassRef'" + classRef
          + "'", exception);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField(PROPERTY_CALENDAR_SPACE, PROPERTY_CALENDAR_SPACE,
        30);
    String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj,";
    hql += " IntegerProperty as int ";
    hql += "where obj.name=doc.fullName ";
    hql += "and not doc.fullName='$doc.getFullName()' ";
    hql += "and obj.className='" + CALENDAR_CONFIG_CLASS + "' ";
    hql += "and int.id.id=obj.id ";
    hql += "and int.id.name='" + PROPERTY_IS_SUBSCRIBABLE + "' ";
    hql += "and int.value='1' ";
    hql += "order by doc.fullName asc";
    needsUpdate |= bclass.addDBListField(PROPERTY_SUBSCRIBE_TO, PROPERTY_SUBSCRIBE_TO, 5,
        true, hql);
    needsUpdate |= bclass.addTextField(PROPERTY_OVERVIEW_COLUMN_CONFIG,
        PROPERTY_OVERVIEW_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addTextField(PROPERTY_EVENT_COLUMN_CONFIG,
        PROPERTY_EVENT_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addNumberField(PROPERTY_EVENT_PER_PAGE, PROPERTY_EVENT_PER_PAGE,
        5, "integer");
    needsUpdate |= bclass.addBooleanField(PROPERTY_HAS_MORE_LINK, PROPERTY_HAS_MORE_LINK,
        "yesno");
    needsUpdate |= bclass.addBooleanField(PROPERTY_IS_SUBSCRIBABLE,
        PROPERTY_IS_SUBSCRIBABLE, "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use {@link ICalendarClassConfig}
   */
  @Deprecated
  public DocumentReference getCalendarClassRef(String wikiName) {
    return classConf.getCalendarClassRef(new WikiReference(wikiName));
  }

  private BaseClass getCalendarEventClass() throws XWikiException {
    DocumentReference classRef = getCalendarEventClassRef(getContext().getDatabase());
    XWikiDocument doc;
    boolean needsUpdate = false;

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (Exception exception) {
      LOGGER.error("Exception while getting doc for ClassRef'" + classRef
          + "'", exception);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField(PROPERTY_LANG, PROPERTY_LANG, 30);
    needsUpdate |= bclass.addTextField(PROPERTY_TITLE, PROPERTY_TITLE, 30);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_TITLE_RTE, PROPERTY_TITLE_RTE, 80,
        15);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_DESCRIPTION, PROPERTY_DESCRIPTION, 80,
        15);
    needsUpdate |= bclass.addTextField(PROPERTY_LOCATION, PROPERTY_LOCATION, 30);
    needsUpdate |= bclass.addTextAreaField(PROPERTY_LOCATION_RTE, PROPERTY_LOCATION_RTE,
        80, 15);
    needsUpdate |= addDateField(bclass, PROPERTY_EVENT_DATE, PROPERTY_EVENT_DATE,
        PROPERTY_EVENT_DATE_FORMAT, 20, 0, getRegexDate(false, true),
        PROPERTY_EVENT_DATE_VALIDATION);
    needsUpdate |= addDateField(bclass, PROPERTY_EVENT_DATE_END, PROPERTY_EVENT_DATE_END,
        PROPERTY_EVENT_DATE_FORMAT, 20, 0, getRegexDate(true, true),
        PROPERTY_EVENT_DATE_END_VALIDATION);
    needsUpdate |= bclass.addBooleanField(PROPERTY_EVENT_IS_SUBSCRIBABLE,
        PROPERTY_EVENT_IS_SUBSCRIBABLE, "yesno");

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use {@link ICalendarClassConfig}
   */
  @Deprecated
  public DocumentReference getCalendarEventClassRef(String wikiName) {
    return classConf.getCalendarEventClassRef(new WikiReference(wikiName));
  }

  private BaseClass getSubscriptionClass() throws XWikiException {
    DocumentReference classRef = getSubscriptionClassRef(getContext().getDatabase());
    XWikiDocument doc;
    boolean needsUpdate = false;

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (Exception exception) {
      LOGGER.error("Exception while getting doc for ClassRef'" + classRef
          + "'", exception);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField(PROPERTY_SUBSCRIBER, PROPERTY_SUBSCRIBER, 30);
    needsUpdate |= bclass.addBooleanField(PROPERTY_DO_SUBSCRIBE, PROPERTY_DO_SUBSCRIBE,
        "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use {@link ICalendarClassConfig}
   */
  @Deprecated
  public DocumentReference getSubscriptionClassRef(String wikiName) {
    return classConf.getSubscriptionClassRef(new WikiReference(wikiName));
  }

  public String getRegexDate(boolean allowEmpty, boolean withTime) {
    String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
    if (withTime) {
      regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9])";
    }
    return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
  }

}
