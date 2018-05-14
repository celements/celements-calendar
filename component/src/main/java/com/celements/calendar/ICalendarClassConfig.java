package com.celements.calendar;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.classes.CalendarClassDefinition;
import com.celements.calendar.classes.CalendarConfigClass;

@Deprecated
@ComponentRole
public interface ICalendarClassConfig {

  public static final Date DATE_LOW = new Date(-62135773200000L);
  public static final Date DATE_HIGH = new Date(253402297140000L);

  public static final String CALENDAR_SPACE = CalendarClassDefinition.SPACE_NAME;

  public static final String CALENDAR_CONFIG_CLASS_SPACE = CALENDAR_SPACE;
  public static final String CALENDAR_CONFIG_CLASS_DOC = CalendarConfigClass.DOC_NAME;
  public static final String CALENDAR_CONFIG_CLASS = CALENDAR_SPACE + "."
      + CALENDAR_CONFIG_CLASS_DOC;

  public static final String PROPERTY_IS_SUBSCRIBABLE = "is_subscribable";
  public static final String PROPERTY_EVENT_PER_PAGE = "event_per_page";
  public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = "overview_column_config";
  public static final String PROPERTY_EVENT_COLUMN_CONFIG = "event_column_config";
  public static final String PROPERTY_HAS_MORE_LINK = "hasMoreLink";
  public static final String PROPERTY_SUBSCRIBE_TO = "subscribe_to";
  public static final String PROPERTY_CALENDAR_SPACE = "calendarspace";

  public static final String CALENDAR_EVENT_CLASS_SPACE = CALENDAR_SPACE;
  public static final String CALENDAR_EVENT_CLASS_DOC = "CalendarEventClass";
  public static final String CALENDAR_EVENT_CLASS = CALENDAR_SPACE + "." + CALENDAR_EVENT_CLASS_DOC;

  public static final String PROPERTY_LANG = "lang";
  public static final String PROPERTY_TITLE = "l_title";
  public static final String PROPERTY_TITLE_RTE = "l_title_rte";
  public static final String PROPERTY_DESCRIPTION = "l_description";
  public static final String PROPERTY_LOCATION = "location";
  public static final String PROPERTY_LOCATION_RTE = "location_rte";
  public static final String PROPERTY_EVENT_DATE = "eventDate";
  public static final String PROPERTY_EVENT_DATE_END = "eventDate_end";
  public static final String PROPERTY_EVENT_IS_SUBSCRIBABLE = "isSubscribable";

  public static final String SUBSCRIPTION_CLASS_SPACE = CALENDAR_SPACE;
  public static final String SUBSCRIPTION_CLASS_DOC = "SubscriptionClass";
  public static final String SUBSCRIPTION_CLASS = CALENDAR_SPACE + "." + SUBSCRIPTION_CLASS_DOC;

  public static final String PROPERTY_SUBSCRIBER = "subscriber";
  public static final String PROPERTY_DO_SUBSCRIBE = "doSubscribe";

  public DocumentReference getCalendarClassRef();

  public DocumentReference getCalendarClassRef(WikiReference wikiRef);

  public DocumentReference getCalendarEventClassRef();

  public DocumentReference getCalendarEventClassRef(WikiReference wikiRef);

  public DocumentReference getSubscriptionClassRef();

  public DocumentReference getSubscriptionClassRef(WikiReference wikiRef);

}
