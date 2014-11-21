package com.celements.calendar.engine;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.calendar.IEvent;
import com.celements.calendar.search.CalendarEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.XWikiContext;

@Component("lucene")
public class CalendarEngineLucene implements ICalendarEngineRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEngineLucene.class);

  private static final String PROGON_ENGINE_LUCENE_TOTALTIME = 
      "progonEngineLuceneTotalTime";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private IEventSearch eventSearch;

  @Requirement("hql")
  private ICalendarEngineRole hqlEngine;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("getEvents: Delegating to CalendarEngineHQL");
    return hqlEngine.getEvents(startDate, isArchive, lang, allowedSpaces);
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit) {
    int resultLimit = searchService.getResultLimit(false);
    if ((offset + limit) <= resultLimit) {
      return searchEvents(null, startDate, isArchive, lang, allowedSpaces).getEventList(
          offset, limit);
    } else {
      LOGGER.debug("getEvents: Delegating to CalendarEngineHQL for offset '" + offset
          + "' and limit '" + limit + "'");
      return hqlEngine.getEvents(startDate, isArchive, lang, allowedSpaces, offset, limit);
    }
  }

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("countEvents: Delegating to CalendarEngineHQL");
    return hqlEngine.countEvents(startDate, isArchive, lang, allowedSpaces);
  }

  public IEvent getFirstEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("getFirstEvent: Delegating to CalendarEngineHQL");
    return hqlEngine.getFirstEvent(startDate, isArchive, lang, allowedSpaces);
  }

  public IEvent getLastEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("getLastEvent: Delegating to CalendarEngineHQL");
    return hqlEngine.getLastEvent(startDate, isArchive, lang, allowedSpaces);
  }

  public EventSearchResult searchEvents(IEventSearchQuery query, Date startDate,
      boolean isArchive, String lang, List<String> allowedSpaces) {
    long startTime = System.currentTimeMillis();
    CalendarEventSearchQuery calSearchQuery;
    if (query == null) {
      calSearchQuery = new CalendarEventSearchQuery(getContext().getDatabase(), 
          startDate, isArchive, lang, allowedSpaces, null, false);
    } else {
      calSearchQuery = new CalendarEventSearchQuery(query, startDate, isArchive, lang, 
          allowedSpaces);
    }
    EventSearchResult searchResult = eventSearch.getSearchResult(calSearchQuery);
    addToTotalTime(startTime, "searchEvents");
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

  void injectHQLEngine(ICalendarEngineRole hqlEngine) {
    this.hqlEngine = hqlEngine;
  }

  void injectSearchService(ILuceneSearchService searchService) {
    this.searchService = searchService;
  }

}
