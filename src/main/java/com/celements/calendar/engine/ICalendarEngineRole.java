package com.celements.calendar.engine;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.IEvent;

@ComponentRole
public interface ICalendarEngineRole {
	
	public long countEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces);
	
	public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces);
	
	public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces, int offset, int limit);

}
