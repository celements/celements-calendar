package com.celements.calendar.search;

import java.util.List;

import com.celements.search.lucene.query.LuceneQueryApi;

public interface IEventSearchQuery {
  
  public String getDatabase();
  
  public List<String> getSortFields();
  
  public boolean skipChecks();

  public LuceneQueryApi getAsLuceneQuery();

}