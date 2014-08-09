package com.celements.calendar.search;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
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

  public EventSearchResult getSearchResult(IEventSearchQuery query) {
    if (query == null) {
      query = new DefaultEventSearchQuery(getContext().getDatabase());
    }
    LuceneQueryApi luceneQuery = query.getAsLuceneQuery();
    List<String> sortFields = query.getSortFields();
    boolean skipChecks = query.skipChecks();
    String queryString = luceneQuery.getQueryString();
    EventSearchResult result = new EventSearchResult(queryString, sortFields, skipChecks,
        getContext());
    LOGGER.info("getSearchResult: for luceneQuery '" + queryString + "' got result '"
        + result + "'");
    return result;
  }

}
