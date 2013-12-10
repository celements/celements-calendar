package com.celements.calendar.search;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.query.LuceneQueryApi;

@ComponentRole
public interface IEventSearch {  
  
  public EventSearchResult getSearchResult(EventSearchQuery query);

  public EventSearchResult getSearchResult(EventSearchQuery query, boolean invertSort);

  public EventSearchResult getSearchResult(LuceneQueryApi query);

  public EventSearchResult getSearchResult(LuceneQueryApi query, boolean invertSort);

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query);

  public EventSearchResult getSearchResultWithoutChecks(LuceneQueryApi query, 
      boolean invertSort);

  public EventSearchResult getSearchResultFromDate(EventSearchQuery query, Date fromDate);

  public EventSearchResult getSearchResultFromDate(LuceneQueryApi query, Date fromDate);
  
  public EventSearchResult getSearchResultUptoDate(EventSearchQuery query, Date uptoDate);

  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate);

}
