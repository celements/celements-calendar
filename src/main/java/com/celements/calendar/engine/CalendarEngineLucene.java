package com.celements.calendar.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.calendar.IEvent;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;

@Component("lucene")
public class CalendarEngineLucene implements ICalendarEngineRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarEngineLucene.class);

  @Requirement
  private IQueryService queryService;

  @Requirement
  private IEventSearch eventSearch;

  @Requirement("hql")
  private ICalendarEngineRole hqlEngine;

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("getEvents: Delegating to CalendarEngineHQL");
    return hqlEngine.getEvents(startDate, isArchive, lang, allowedSpaces);
  }

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit) {
    return getEvents(queryService.createQuery(), startDate, isArchive, lang,
        allowedSpaces, offset, limit);
  }

  public List<IEvent> getEvents(LuceneQueryApi query, Date startDate, boolean isArchive,
      String lang, List<String> allowedSpaces, int offset, int limit) {
    return getEventSearchResult(query, isArchive, startDate, lang, allowedSpaces
        ).getEventList(offset, limit);
  }

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces) {
    LOGGER.debug("countEvents: Delegating to CalendarEngineHQL");
    return hqlEngine.countEvents(startDate, isArchive, lang, allowedSpaces);
  }

  private EventSearchResult getEventSearchResult(LuceneQueryApi query, boolean isArchive,
      Date startDate, String lang, List<String> allowedSpaces) {
    if (query == null) {
      query = queryService.createQuery();
    }
    addLangRestriction(query, lang);
    addSpaceRestrictions(query, allowedSpaces);
    EventSearchResult searchResult;
    if (!isArchive) {
      searchResult = eventSearch.getSearchResultFromDate(query, startDate);
    } else {
      searchResult = eventSearch.getSearchResultUptoDate(query, startDate);
    }
    LOGGER.debug("getEventSearchResult: " + searchResult);
    return searchResult;
  }

  private void addSpaceRestrictions(LuceneQueryApi query, List<String> allowedSpaces) {
    if (!allowedSpaces.isEmpty()) {
      List<LuceneQueryRestrictionApi> spaceRestrictionList =
          new ArrayList<LuceneQueryRestrictionApi>();
      for (String space : allowedSpaces) {
        spaceRestrictionList.add(queryService.createRestriction("space", space));
      }
      query.addOrRestrictionList(spaceRestrictionList);
    }
  }

  private void addLangRestriction(LuceneQueryApi query, String lang) {
    LuceneQueryRestrictionApi langRestriction = queryService.createRestriction(
        CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_LANG, lang);
    query.addRestriction(langRestriction);
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  void injectEventSearch(IEventSearch eventSearch) {
    this.eventSearch = eventSearch;
  }

  void injectHQLEngine(ICalendarEngineRole hqlEngine) {
    this.hqlEngine = hqlEngine;
  }

}
