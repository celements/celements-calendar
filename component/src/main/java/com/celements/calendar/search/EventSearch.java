package com.celements.calendar.search;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    return getSearchResult(query.getAsLuceneQuery(), null, false);
  }

  public EventSearchResult getSearchResult(EventSearchQuery query, 
      List<String> sortFields) {
    return getSearchResult(query.getAsLuceneQuery(), sortFields, false);
  }

  public EventSearchResult getSearchResult(LuceneQueryApi query) {
    return getSearchResult(query, null, false);
  }

  public EventSearchResult getSearchResult(LuceneQueryApi query, List<String> sortFields) {
    return getSearchResult(query, sortFields, false);
  }

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query) {
    return getSearchResult(query, null, true);
  }

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query, 
      List<String> sortFields) {
    return getSearchResult(query, sortFields, true);
  }
  
  private EventSearchResult getSearchResult(LuceneQueryApi query, List<String> sortFields, 
      boolean skipChecks) {
    query.addRestriction(createEventObjectRestriction());
    sortFields = (sortFields == null ? getDefaultSortFields(false) : sortFields);
    return new EventSearchResult(query.getQueryString(), sortFields, skipChecks, 
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
    List<String> sortFields = getDefaultSortFields(false);
    LOGGER.debug("getSearchResultFromDate: query [" + queryString + "] and sortFields ["
        + sortFields + "]");
    return new EventSearchResult(queryString, sortFields, false, getContext());
  }
  
  public EventSearchResult getSearchResultUptoDate(EventSearchQuery query, Date uptoDate) {
    return getSearchResultUptoDate(query.getAsLuceneQuery(), uptoDate);
  }

  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate) {
    query.addRestriction(createEventObjectRestriction());
    query.addRestriction(queryService.createToDateRestriction(getEventDateFieldName(), 
        uptoDate, false));
    String queryString = query.getQueryString();
    List<String> sortFields = getDefaultSortFields(true);
    LOGGER.debug("getSearchResultUptoDate: query [" + queryString + "] and sortFields ["
        + sortFields + "]");
    return new EventSearchResult(queryString, sortFields, false, getContext());
  }

  private LuceneQueryRestrictionApi createEventObjectRestriction() {
    return queryService.createRestriction("object", "\""
        + CalendarClasses.CALENDAR_EVENT_CLASS + "\"");
  }

  private String getEventDateFieldName() {
    return CalendarClasses.CALENDAR_EVENT_CLASS + "."
        + CalendarClasses.PROPERTY_EVENT_DATE;
  }

  private List<String> getDefaultSortFields(boolean inverted) {
    String pref = (inverted ? "-" : "") + CalendarClasses.CALENDAR_EVENT_CLASS + ".";
    return Arrays.asList(new String[] { pref + CalendarClasses.PROPERTY_EVENT_DATE,
        pref + CalendarClasses.PROPERTY_EVENT_DATE_END,
        pref + CalendarClasses.PROPERTY_TITLE });
  }

  void injectQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

}
