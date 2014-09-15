package com.celements.calendar.search;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.XWikiContext;

@Component
public class EventSearch implements IEventSearch {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      EventSearch.class);

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public EventSearchResult getSearchResult(IEventSearchQuery query) {
    if (query == null) {
      query = new DefaultEventSearchQuery(getContext().getDatabase());
    }
    LuceneQuery luceneQuery = query.getAsLuceneQuery();
    List<String> sortFields = query.getSortFields();
    boolean skipChecks = query.skipChecks();
    EventSearchResult result = new EventSearchResult(luceneQuery, sortFields, skipChecks);
    LOGGER.info("getSearchResult: for luceneQuery '" + luceneQuery + "' got result '"
        + result + "'");
    return result;
  }

}
