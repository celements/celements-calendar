package com.celements.calendar.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.web.service.IWebUtilsService;

@Component
public class EventSearchService implements IEventSearchRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventSearchService.class);

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ConfigurationSource configSource;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  public boolean skipChecks() {
    boolean skipChecks = configSource.getProperty("calendar.search.skipChecks", 
        Boolean.class);
    LOGGER.debug("skipChecks: got '{}'", skipChecks);
    return skipChecks;
  }

  @Override
  public EventSearchResult getSearchResult(IEventSearchQuery query) {
    return getSearchResult(query, skipChecks());
  }

  @Override
  public EventSearchResult getSearchResult(IEventSearchQuery query, boolean skipChecks) {
    if (query == null) {
      query = new DefaultEventSearchQuery(webUtilsService.getWikiRef());
    }
    LuceneQuery luceneQuery = query.getAsLuceneQuery();
    List<String> sortFields = query.getSortFields();
    EventSearchResult result = new EventSearchResult(luceneQuery, sortFields, skipChecks);
    LOGGER.info("getSearchResult: for query '{}' got result '{}'", luceneQuery, result);
    return result;
  }

  void injectConfigSource(ConfigurationSource configSource) {
    this.configSource = configSource;
  }

}
