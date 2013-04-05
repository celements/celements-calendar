package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;
import com.xpn.xwiki.web.Utils;

public class EventSearchResult {

  private IWebUtilsService webUtils;

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      EventSearchResult.class);

  private SearchResults searchResultsCache;
  private LucenePlugin lucenePlugin;

  private final String luceneQuery;
  private final String[] sortFields;
  private final XWikiContext context;

  EventSearchResult(String luceneQuery, String[] sortFields, XWikiContext context) {
    this.luceneQuery = luceneQuery;
    this.sortFields = sortFields;
    this.context = context;
  }

  public String getLuceneQuery() {
    return luceneQuery;
  }

  public String[] getSortFields() {
    return Arrays.copyOf(sortFields, sortFields.length);
  }

  /**
   * 
   * @return all events
   */
  public List<IEvent> getEventList() {
    return getEventList(0, 0);
  }

  /**
   * 
   * @param offset from 0 to (size - 1)
   * @param limit all remaining events are returned for values < 0 or >= (size - 1)
   * @return selected events
   */
  public List<IEvent> getEventList(int offset, int limit) {
    SearchResults results = luceneSearch();
    if (results != null) {
      List<SearchResult> list;
      if (limit > 0) {
        list = results.getResults(offset + 1, limit);
      } else {
        list = results.getResults(offset + 1, results.getHitcount());
      }
      return convertToEventList(list);
    } else {
      return null;
    }
  }

  private List<IEvent> convertToEventList(List<SearchResult> results) {
    List<IEvent> eventList = new ArrayList<IEvent>();
    for (SearchResult result : results) {
      DocumentReference eventDocRef = getWebUtils().resolveDocumentReference(
          result.getFullName());
      eventList.add(new Event(eventDocRef));
    }
    return eventList;
  }

  public int getSize() {
    return luceneSearch().getHitcount();
  }

  private SearchResults luceneSearch() {
    if (searchResultsCache == null) {
      try {
        searchResultsCache = getLucenePlugin().getSearchResults(luceneQuery, sortFields,
            null, "default,de", context);
      } catch (Exception exception) {
        LOGGER.error("Unable to get lucene search results for query '" + luceneQuery
            + "'", exception);
      }
    }
    return searchResultsCache;
  }

  private LucenePlugin getLucenePlugin() {
    if (lucenePlugin == null) {
      lucenePlugin = (LucenePlugin) context.getWiki().getPlugin("lucene", context);
    }
    return lucenePlugin;
  }

  private IWebUtilsService getWebUtils() {
    if(webUtils == null){
      webUtils = Utils.getComponent(IWebUtilsService.class);
    }
    return webUtils;
  }

  @Override
  public String toString() {
    return "EventSearchResult [luceneQuery=" + luceneQuery + ", sortFields="
        + Arrays.toString(sortFields) + "]";
  }

  void injectLucenePlugin(LucenePlugin lucenePlugin) {
    this.lucenePlugin = lucenePlugin;
  }

}
