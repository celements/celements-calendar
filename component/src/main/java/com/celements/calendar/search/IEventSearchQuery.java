package com.celements.calendar.search;

import java.util.List;

import org.xwiki.model.reference.WikiReference;

import com.celements.search.lucene.query.LuceneQuery;

public interface IEventSearchQuery {

  /**
   * @deprecated instead use {@link #getWikiRef()}
   * @return
   */
  @Deprecated
  public String getDatabase();
  
  public WikiReference getWikiRef();
  
  public List<String> getSortFields();

  public LuceneQuery getAsLuceneQuery();

}
