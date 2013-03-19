package com.celements.calendar.search;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.query.LuceneQueryApi;

@ComponentRole
public interface IEventSearch {
  
  public EventSearchResult getSearchResult(LuceneQueryApi query);
  
  public EventSearchResult getSearchResultFromDate(LuceneQueryApi query, Date fromDate);
  
  public EventSearchResult getSearchResultUptoDate(LuceneQueryApi query, Date uptoDate);

}
