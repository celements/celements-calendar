package com.celements.calendar.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.calendar.Event;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWikiContext;

@Component
public class EventSearch implements IEventSearch {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
  private static final String DATE_LOW = "000101010000";
  private static final String DATE_HIGH = "999912312359";
  
  @Requirement
  private IQueryService queryService;
  
  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public EventSearchResult getSearchResult(LuceneQueryApi query) {
    query.addRestriction(createEventObjectRestriction());
    return new EventSearchResult(query.getQueryString(), 
        new String[] { getEventDateFieldName() }, getContext());
  }

  public EventSearchResult getSearchResultFromDate(LuceneQueryApi query, Date fromDate) {
    query.addRestriction(createEventObjectRestriction());
    query.addRestriction(queryService.createRangeRestriction(getEventDateFieldName(), 
        SDF.format(fromDate), DATE_HIGH, true));
    return new EventSearchResult(query.getQueryString(), 
        new String[] { getEventDateFieldName() }, getContext());
  }

  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate) {
    query.addRestriction(createEventObjectRestriction());
    query.addRestriction(queryService.createRangeRestriction(getEventDateFieldName(), 
        DATE_LOW, SDF.format(uptoDate), false));   
    return new EventSearchResult(query.getQueryString(), 
        new String[] { "-" + getEventDateFieldName() }, getContext());
  }
  
  private LuceneQueryRestrictionApi createEventObjectRestriction() {
    return queryService.createRestriction("object", Event.CLASS);
  }
  
  private String getEventDateFieldName() {
    return Event.CLASS + "." + Event.PROPERTY_EVENT_DATE;
  }

}
