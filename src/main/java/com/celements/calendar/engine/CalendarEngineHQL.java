package com.celements.calendar.engine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.web.service.IWebUtilsService;

@Component("hql")
public class CalendarEngineHQL implements ICalendarEngineRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarEngineHQL.class);

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IWebUtilsService webUtilsService;

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, true);
      List<Object> eventCount = queryManager.createQuery(query, Query.HQL).execute();
      if((eventCount != null) && (eventCount.size() > 0)) {
        return (Long) eventCount.get(0);
      }
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
    }
    return 0;
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, false);
      List<String> eventDocs = queryManager.createQuery(query, Query.HQL).execute();
      List<IEvent> eventList = convertToEventList(eventDocs);
      if (eventList == null) {
        LOGGER.error("prevent returning null in getEvents of CalendarEngineHQL for"
            + " startDate [" + startDate + "], isArchive [" + isArchive + "], lang ["
            + lang + "], allowedSpaces [" + allowedSpaces + "], eventDocs [" + eventDocs
            + "], query [" + query + "].");
        eventList = Collections.emptyList();
      }
      return eventList;
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
    }
    return Collections.emptyList();
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit) {
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, false);
      List<String> eventDocs = queryManager.createQuery(query, Query.HQL).setOffset(offset
          ).setLimit(limit).execute();
      List<IEvent> eventList = convertToEventList(eventDocs);
      if (eventList == null) {
        LOGGER.error("prevent returning null in getEvents of CalendarEngineHQL for"
            + " startDate [" + startDate + "], isArchive [" + isArchive + "], lang ["
            + lang + "], allowedSpaces [" + allowedSpaces + "], offset [" + offset
            + "], limit [" + limit + "], eventDocs [" + eventDocs + "], query [" + query
            + "].");
        eventList = Collections.emptyList();
      }
      return eventList;
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
    }
    return Collections.emptyList();
  }

  private List<IEvent> convertToEventList(List<String> eventDocs) {
    List<IEvent> eventList = new ArrayList<IEvent>();
    for (String eventDocName : eventDocs) {
      eventList.add(new Event(webUtilsService.resolveDocumentReference(eventDocName)));
    }
    return eventList;
  }

  private String getQuery(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces,	boolean count) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String timeComp = ">=";
    String sortOrder = "asc";
    String selectEmptyDates = "or ec.eventDate is null";
    if(isArchive){
      timeComp = "<";
      sortOrder = "desc";
      selectEmptyDates = "";
    }
    String hql = "select ";
    if(count){
      hql += "count(obj.name)";
    } else {
      hql += "obj.name";
    }
    hql += " from BaseObject as obj, " + CalendarClasses.CALENDAR_EVENT_CLASS + " as ec";
    hql += " where ec.id.id=obj.id and obj.className = '"
        + CalendarClasses.CALENDAR_EVENT_CLASS + "' ";
    hql += "and ec.lang='" + lang + "' ";
    hql += "and (ec.eventDate " + timeComp + " '" + format.format(startDate) + "' "
        + selectEmptyDates + ") ";
    hql += "and " + getAllowedSpacesHQL(allowedSpaces);
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    hql += ", ec.l_title " + sortOrder;
    LOGGER.debug(hql);

    return hql;
  }

  private String getAllowedSpacesHQL(List<String> allowedSpaces) {
    String spaceHQL = "";
    for (String space : allowedSpaces) {
      if (spaceHQL.length() > 0) {
        spaceHQL += " or ";
      }
      spaceHQL += "obj.name like '" + space + ".%'";
    }
    if (spaceHQL.length() > 0) {
      spaceHQL = "(" + spaceHQL + ")";
    } else {
      spaceHQL = "(obj.name like '.%')";
    }
    return spaceHQL;
  }

  public IEvent getFirstEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    IEvent event = getBorderEvent(true, startDate, isArchive, lang, allowedSpaces);
    if (event == null) {
      LOGGER.debug("getFirstEventDate: no Events for startDate '" + startDate
          + "', isArchive '" + isArchive + "', language '" + lang
          + "' and allowedSpaces '" + allowedSpaces + "'");
    }
    return event;
  }

  public IEvent getLastEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    IEvent event = getBorderEvent(false, startDate, isArchive, lang, allowedSpaces);
    if (event == null) {
      LOGGER.debug("getLastEventDate: no Events for startDate '" + startDate
          + "', isArchive '" + isArchive + "', language '" + lang
          + "' and allowedSpaces '" + allowedSpaces + "'");
    }
    return event;
  }

  private IEvent getBorderEvent(boolean first, Date startDate, boolean isArchive,
      String lang, List<String> allowedSpaces) {
    int start = 0;
    if ((first && isArchive) || (!first && !isArchive)) {
      long count = countEvents(startDate, isArchive, lang, allowedSpaces);
      start = (int) (count - 1);
    }
    if (start >= 0) {
      List<IEvent> events = getEvents(startDate, isArchive, lang, allowedSpaces, start, 1);
      if (events.size() > 0) {
        return events.get(0);
      }
    }
    return null;
  }

  public EventSearchResult searchEvents(EventSearchQuery query, Date startDate,
      boolean isArchive, String lang, List<String> allowedSpaces) {
    throw new UnsupportedOperationException("searchEvents not supported for " +
        "CalendarEngineHQL");
  }

  void injectQueryManager(QueryManager queryManagerMock) {
    this.queryManager = queryManagerMock;
  }

}
