package com.celements.calendar.search;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWikiContext;

@Component
public class EventSearch implements IEventSearch {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      EventSearch.class);

  @Requirement
  private IQueryService queryService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public EventSearchResult getSearchResult(EventSearchQuery query) {
    return getSearchResult(query.getAsLuceneQuery(), false);
  }

  public EventSearchResult getSearchResult(EventSearchQuery query, boolean invertSort) {
    return getSearchResult(query.getAsLuceneQuery(), invertSort);
  }

  public EventSearchResult getSearchResult(LuceneQueryApi query) {
    return getSearchResult(query, false);
  }

  public EventSearchResult getSearchResult(LuceneQueryApi query, boolean invertSort) {
    return getSearchResult(query, invertSort);
  }

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query) {
    return getSearchResult(query, false);
  }

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query, 
      boolean invertSort) {
    return getSearchResult(query, invertSort);
  }
  
  private EventSearchResult getSearchResult_int(LuceneQueryApi query, boolean invertSort) {
    query.addRestriction(createEventObjectRestriction());
    return new EventSearchResult(query.getQueryString(), getSortFields(invertSort), 
        getContext());
  }

  public EventSearchResult getSearchResultFromDate(EventSearchQuery query, Date fromDate) {
    return getSearchResultFromDate(query.getAsLuceneQuery(), fromDate);
  }


  public EventSearchResult getSearchResultFromDate(LuceneQueryApi query, Date fromDate) {
    query.addRestriction(createEventObjectRestriction());
    query.addRestriction(queryService.createFromDateRestriction(getEventDateFieldName(), 
        fromDate, true));
    String queryString = query.getQueryString();
    LOGGER.debug("getSearchResultFromDate: query [" + queryString + "] and sortFields ["
        + getSortFields(false) + "].");
    return new EventSearchResult(queryString, getSortFields(false), getContext());
  }
  public EventSearchResult getSearchResultUptoDate(EventSearchQuery query, Date uptoDate) {
    return getSearchResultUptoDate(query.getAsLuceneQuery(), uptoDate);
  }

  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate) {
    query.addRestriction(createEventObjectRestriction());
    query.addRestriction(queryService.createToDateRestriction(getEventDateFieldName(), 
        uptoDate, false));
    String queryString = query.getQueryString();
    LOGGER.debug("getSearchResultUptoDate: query [" + queryString + "] and sortFields ["
        + getSortFields(true) + "].");
    return new EventSearchResult(queryString, getSortFields(true), getContext());
  }

  private LuceneQueryRestrictionApi createEventObjectRestriction() {
    return queryService.createRestriction("object", "\""
        + CalendarClasses.CALENDAR_EVENT_CLASS + "\"");
  }

  private String getEventDateFieldName() {
    return CalendarClasses.CALENDAR_EVENT_CLASS + "."
        + CalendarClasses.PROPERTY_EVENT_DATE;
  }

  private String[] getSortFields(boolean inverted) {
    String pref = (inverted ? "-" : "") + CalendarClasses.CALENDAR_EVENT_CLASS + ".";
    return new String[] { pref + CalendarClasses.PROPERTY_EVENT_DATE,
        pref + CalendarClasses.PROPERTY_EVENT_DATE_END,
        pref + CalendarClasses.PROPERTY_TITLE };
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

}
