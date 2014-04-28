package com.celements.calendar.search;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.query.LuceneQueryApi;

@ComponentRole
public interface IEventSearch {  
  
  public EventSearchResult getSearchResult(EventSearchQuery query);

  public EventSearchResult getSearchResult(EventSearchQuery query, 
      List<String> sortFields);

  public EventSearchResult getSearchResult(LuceneQueryApi query);

  public EventSearchResult getSearchResult(LuceneQueryApi query, 
      List<String> sortFields);

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query);

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query, 
      List<String> sortFields);

  public EventSearchResult getSearchResultFromDate(EventSearchQuery query, Date fromDate);

  public EventSearchResult getSearchResultFromDate(LuceneQueryApi query, Date fromDate);
  
  public EventSearchResult getSearchResultUptoDate(EventSearchQuery query, Date uptoDate);

  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate);

}
