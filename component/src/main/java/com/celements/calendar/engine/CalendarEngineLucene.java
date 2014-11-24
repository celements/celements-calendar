package com.celements.calendar.engine;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.CalendarEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;

@Component(CalendarEngineLucene.NAME)
public class CalendarEngineLucene implements ICalendarEngineRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEngineLucene.class);

  private static final String PROGON_ENGINE_LUCENE_TOTALTIME = 
      "progonEngineLuceneTotalTime";

  public static final String NAME = "lucene";

  @Requirement
  private IEventSearch eventSearch;

  @Requirement
  private Execution execution;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<IEvent> getEvents(ICalendar cal, int offset, int limit) {
    long startTime = System.currentTimeMillis();
    List<IEvent> eventList = Collections.emptyList();
    try {
      eventList = searchEvents(cal, null).getEventList(offset, limit);
    } catch (LuceneSearchException lse) {
      LOGGER.error("Unable to search for cal '" + cal + "'", lse);
    }
    addToTotalTime(startTime, "getEvents");
    return eventList;
  }

  @Override
  public long countEvents(ICalendar cal) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try {
      count = searchEvents(cal, null).getSize();
    } catch (LuceneSearchException lse) {
      LOGGER.error("Unable to search for cal '" + cal + "'", lse);
    }
    addToTotalTime(startTime, "countEvents");
    return count;
  }

  @Override
  public IEvent getFirstEvent(ICalendar cal) {
    IEvent event = null;
    try {
      event = getBorderEvent(cal, true);
    } catch (LuceneSearchException lse) {
      LOGGER.error("Unable to search for cal '" + cal + "'", lse);
    }
    return event;
  }

  @Override
  public IEvent getLastEvent(ICalendar cal) {
    IEvent event = null;
    try {
      event = getBorderEvent(cal, false);
    } catch (LuceneSearchException lse) {
      LOGGER.error("Unable to search for cal '" + cal + "'", lse);
    }
    return event;
  }

  private IEvent getBorderEvent(ICalendar cal, boolean first) throws LuceneSearchException {
    long startTime = System.currentTimeMillis();
    IEvent ret = null;
    EventSearchResult result = searchEvents(cal, null);
    int offset = 0;
    if ((first && cal.isArchive()) || (!first && !cal.isArchive())) {
      offset = (int) (result.getSize() - 1);
    }
    List<IEvent> events = result.getEventList(offset, 1);
    if (events.size() > 0) {
      ret = events.get(0);
    } else {
      LOGGER.debug("getFirst/LastEvent: no Event for cal '" + cal + "', first '" + first 
          + "'");
    }
    addToTotalTime(startTime, "getBorderEvent");
    return ret;
  }

  public EventSearchResult searchEvents(ICalendar cal, IEventSearchQuery query) {
    CalendarEventSearchQuery calSearchQuery;
    if (query == null) {
      calSearchQuery = new CalendarEventSearchQuery(cal, Collections.<String>emptyList());
    } else {
      calSearchQuery = new CalendarEventSearchQuery(cal, query);
    }
    EventSearchResult searchResult = eventSearch.getSearchResult(calSearchQuery);
    LOGGER.debug("searchEvents: " + searchResult);
    return searchResult;
  }
  
  private void addToTotalTime(long startTime, String methodName) {
    long time = System.currentTimeMillis() - startTime;
    Long totalTime = 0L;
    if (execution.getContext().getProperty(PROGON_ENGINE_LUCENE_TOTALTIME) != null) {
      totalTime = (Long) execution.getContext().getProperty(PROGON_ENGINE_LUCENE_TOTALTIME);
    }
    totalTime += time;
    execution.getContext().setProperty(PROGON_ENGINE_LUCENE_TOTALTIME, totalTime);
    LOGGER.debug("{}: finished in {}ms, total time {}ms", methodName, time, totalTime);
  }

  void injectEventSearch(IEventSearch eventSearch) {
    this.eventSearch = eventSearch;
  }

}
