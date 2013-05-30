package com.celements.calendar.engine;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.IEvent;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;

@ComponentRole
public interface ICalendarEngineRole {

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit);

  public IEvent getFirstEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public IEvent getLastEvent(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public EventSearchResult searchEvents(EventSearchQuery query, Date startDate,
      boolean isArchive, String lang, List<String> allowedSpaces);

}
