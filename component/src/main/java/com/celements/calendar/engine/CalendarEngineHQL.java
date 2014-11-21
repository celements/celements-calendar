package com.celements.calendar.engine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;

@Component("hql")
public class CalendarEngineHQL implements ICalendarEngineRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEngineHQL.class);

  private static final String PROGON_ENGINE_HQL_TOTALTIME = "progonEngineHQLTotalTime";

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, true);
      List<Object> eventCount = queryManager.createQuery(query, Query.HQL).execute();
      if((eventCount != null) && (eventCount.size() > 0)) {
        count = (Long) eventCount.get(0);
      }
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
    }
    addToTotalTime(startTime, "countEvents");
    return count;
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    long startTime = System.currentTimeMillis();
    List<IEvent> eventList;
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, false);
      List<String> eventDocs = queryManager.createQuery(query, Query.HQL).execute();
      eventList = convertToEventList(eventDocs);
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
      eventList = Collections.emptyList();
    }
    addToTotalTime(startTime, "getEvents");
    return eventList;
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit) {
    long startTime = System.currentTimeMillis();
    List<IEvent> eventList;
    try {
      String query = getQuery(startDate, isArchive, lang,	allowedSpaces, false);
      List<String> eventDocs = queryManager.createQuery(query, Query.HQL).setOffset(offset
          ).setLimit(limit).execute();
      eventList = convertToEventList(eventDocs);
    } catch (QueryException queryException) {
      LOGGER.error("getEvents: " + queryException);
      eventList = Collections.emptyList();
    }
    addToTotalTime(startTime, "getEvents");
    return eventList;
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
    hql += "and ec.lang='" + lang + "' and (";
    if(toArchiveOnEndDate()) {
      hql += "COALESCE(ec.eventDate_end, ec.eventDate)";
    } else {
      hql += "ec.eventDate";
    }
    hql += " " + timeComp + " '" + format.format(startDate) + "' "
        + selectEmptyDates + ") ";
    hql += "and " + getAllowedSpacesHQL(allowedSpaces);
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    hql += ", ec.l_title " + sortOrder;
    LOGGER.debug(hql);

    return hql;
  }

  boolean toArchiveOnEndDate() {
    return getContext().getWiki().getXWikiPreferenceAsInt("calendar_toArchiveOnEndDate", 
        "celements.calendar.toArchiveOnEndDate", 1, getContext()) == 1;
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

  public EventSearchResult searchEvents(IEventSearchQuery query, Date startDate,
      boolean isArchive, String lang, List<String> allowedSpaces) {
    throw new UnsupportedOperationException("searchEvents not supported for " +
        "CalendarEngineHQL");
  }
  
  private void addToTotalTime(long startTime, String methodName) {
    long time = System.currentTimeMillis() - startTime;
    Long totalTime = 0L;
    if (execution.getContext().getProperty(PROGON_ENGINE_HQL_TOTALTIME) != null) {
      totalTime = (Long) execution.getContext().getProperty(PROGON_ENGINE_HQL_TOTALTIME);
    }
    totalTime += time;
    execution.getContext().setProperty(PROGON_ENGINE_HQL_TOTALTIME, totalTime);
    LOGGER.debug("{}: finished in {}ms, total time {}ms", methodName, time, totalTime);
  }

  void injectQueryManager(QueryManager queryManagerMock) {
    this.queryManager = queryManagerMock;
  }

}
