package com.celements.calendar.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;

@Component(CalendarEngineHQL.NAME)
public class CalendarEngineHQL implements ICalendarEngineRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEngineHQL.class);

  private static final String PROGON_ENGINE_HQL_TOTALTIME = "progonEngineHQLTotalTime";
  private static final DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  public static final String NAME = "hql";

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

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public long countEvents(ICalendar cal) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try {
      Query query = getQuery(cal, true);
      List<Object> eventCount = query.execute();
      if((eventCount != null) && (eventCount.size() > 0)) {
        count = (Long) eventCount.get(0);
      }
    } catch (QueryException queryException) {
      LOGGER.error("Unable to execute query for cal '" + cal + "'", queryException);
    }
    addToTotalTime(startTime, "countEvents");
    return count;
  }

  @Override
  public List<IEvent> getEvents(ICalendar cal, int offset, int limit) {
    long startTime = System.currentTimeMillis();
    List<IEvent> eventList = Collections.emptyList();
    try {
      Query query = getQuery(cal, false);
      if (offset > 0) {
        query.setOffset(offset);
      }
      if (limit > 0) {
        query.setLimit(limit);
      }
      eventList = convertToEventList(cal.getWikiRef(), query.<String>execute());
    } catch (QueryException queryException) {
      LOGGER.error("Unable to execute query for cal '" + cal + "'", queryException);
    }
    addToTotalTime(startTime, "getEvents");
    return eventList;
  }

  private List<IEvent> convertToEventList(WikiReference wikiRef, List<String> eventDocs) {
    List<IEvent> eventList = new ArrayList<IEvent>();
    for (String docName : eventDocs) {
      eventList.add(new Event(webUtilsService.resolveDocumentReference(docName, wikiRef)));
    }
    return eventList;
  }

  private Query getQuery(ICalendar cal, boolean asCount) throws QueryException {
    String evClassName = CalendarClasses.CALENDAR_EVENT_CLASS;
    String timeComp = ">=";
    String sortOrder = "asc";
    String selectEmptyDates = "or ec.eventDate is null";
    if(cal.isArchive()){
      timeComp = "<";
      sortOrder = "desc";
      selectEmptyDates = "";
    }
    String hql = "select ";
    if(asCount){
      hql += "count(obj.name) ";
    } else {
      hql += "obj.name ";
    }
    hql += "from BaseObject as obj, " + evClassName + " as ec ";
    hql += "where ec.id.id=obj.id and obj.className = '" + evClassName + "' ";
    hql += "and ec.lang='" + cal.getLanguage() + "' and (";
    if(toArchiveOnEndDate()) {
      hql += "COALESCE(ec.eventDate_end, ec.eventDate)";
    } else {
      hql += "ec.eventDate";
    }
    hql += " " + timeComp + " '" + SDF.format(cal.getStartDate()) + "' "
        + selectEmptyDates + ") ";
    hql += "and " + getAllowedSpacesHQL(cal.getAllowedSpaces());
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    hql += ", ec.l_title " + sortOrder;
    LOGGER.debug(hql);
    Query query = queryManager.createQuery(hql, Query.HQL);
    query.setWiki(cal.getWikiRef().getName());
    // TODO bind values
    return query;
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

  @Override
  public IEvent getFirstEvent(ICalendar cal) {
    return getBorderEvent(cal, true);
  }

  @Override
  public IEvent getLastEvent(ICalendar cal) {
    return getBorderEvent(cal, false);
  }

  private IEvent getBorderEvent(ICalendar cal, boolean first) {
    IEvent ret = null;
    int offset = 0;
    if ((first && cal.isArchive()) || (!first && !cal.isArchive())) {
      long count = countEvents(cal);
      offset = (int) (count - 1);
    }
    if (offset >= 0) {
      List<IEvent> events = getEvents(cal, offset, 1);
      if (events.size() > 0) {
        ret = events.get(0);
      }
    }
    if (ret == null) {
      LOGGER.debug("getFirst/LastEvent: no Event for cal '" + cal + "', first '" + first 
          + "'");
    }
    return ret;
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
