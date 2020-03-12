package com.celements.calendar.engine;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.CalendarEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.ICalendarSearchQueryBuilder;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.IEventSearchRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;

@Component(CalendarEngineLucene.NAME)
public class CalendarEngineLucene extends AbstractCalendarEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEngineLucene.class);

  private static final String PROGON_ENGINE_LUCENE_TOTALTIME = "progonEngineLuceneTotalTime";

  public static final String NAME = "lucene";

  @Requirement
  private IEventSearchRole eventSearchService;

  @Requirement
  private ILuceneSearchService searchService;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public long getEngineLimit() {
    return searchService.getResultLimit(eventSearchService.skipChecks());
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
  protected long countEventsInternal(ICalendar cal) {
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
    IEvent ret = null;
    int offset = 0;
    if ((first && cal.isArchive()) || (!first && !cal.isArchive())) {
      offset = (int) (countEvents(cal) - 1);
      ;
    }
    List<IEvent> events = getEvents(cal, offset, 1);
    if (events.size() > 0) {
      ret = events.get(0);
    } else {
      LOGGER.debug("getFirst/LastEvent: no Event for cal '" + cal + "', first '" + first + "'");
    }
    return ret;
  }

  public EventSearchResult searchEvents(ICalendar cal, IEventSearchQuery queryBuilder) {
    ICalendarSearchQueryBuilder calSearchQuery;
    if (queryBuilder == null) {
      calSearchQuery = new CalendarEventSearchQuery(cal.getDocumentReference().getWikiReference());
      // } else if (query instanceof ICalendarEventSearchQuery) {
      // calSearchQuery = (ICalendarEventSearchQuery) query;
    } else {
      calSearchQuery = new CalendarEventSearchQuery(queryBuilder);
    }
    calSearchQuery.addCalendarRestrictions(cal);
    EventSearchResult searchResult = eventSearchService.getSearchResult(calSearchQuery);
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

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  void injectEventSearchService(IEventSearchRole eventSearchService) {
    this.eventSearchService = eventSearchService;
  }

  void injectSearchService(ILuceneSearchService searchService) {
    this.searchService = searchService;
  }

}
