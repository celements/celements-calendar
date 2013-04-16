package com.celements.calendar.engine;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.IEvent;
import com.celements.search.lucene.query.LuceneQueryApi;

@ComponentRole
public interface ICalendarEngineRole {

  public long countEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces);

  public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
      List<String> allowedSpaces, int offset, int limit);

  public List<IEvent> getEvents(LuceneQueryApi query, Date startDate, boolean isArchive,
      String lang, List<String> allowedSpaces, int offset, int limit);

  public Date getFirstEventDate(String lang, List<String> allowedSpaces);

  public Date getLastEventDate(String lang, List<String> allowedSpaces);

}
