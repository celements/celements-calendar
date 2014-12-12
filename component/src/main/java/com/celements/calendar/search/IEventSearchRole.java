package com.celements.calendar.search;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IEventSearchRole {

  public boolean skipChecks();

  public EventSearchResult getSearchResult(IEventSearchQuery query);

  public EventSearchResult getSearchResult(IEventSearchQuery query, boolean skipChecks);

}
