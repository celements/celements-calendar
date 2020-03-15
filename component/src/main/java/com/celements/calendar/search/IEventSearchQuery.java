package com.celements.calendar.search;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.WikiReference;

import com.celements.search.lucene.query.LuceneQuery;

public interface IEventSearchQuery {

  /**
   * @deprecated instead use {@link #getWikiRef()}
   * @return
   */
  @Deprecated
  public @Nullable String getDatabase();

  public @NotNull WikiReference getWikiRef();

  public @NotNull IEventSearchQuery setSortFields(@NotNull List<String> sortFields);

  public @NotNull List<String> getSortFields();

  public @NotNull LuceneQuery getAsLuceneQuery();

}
