package com.celements.calendar.engine;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;

@ComponentRole
public interface ICalendarEngineRole {
  
  public String getName();
  
  public long countEvents(ICalendar cal);

  public List<IEvent> getEvents(ICalendar cal, int offset, int limit);

  public IEvent getFirstEvent(ICalendar cal);

  public IEvent getLastEvent(ICalendar cal);

}
