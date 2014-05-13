package com.celements.calendar.search;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IEventSearch {

  public EventSearchResult getSearchResult(IEventSearchQuery query);

}
