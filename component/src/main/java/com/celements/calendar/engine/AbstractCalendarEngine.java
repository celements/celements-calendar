package com.celements.calendar.engine;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.calendar.ICalendar;
import com.celements.web.service.IWebUtilsService;

public abstract class AbstractCalendarEngine implements ICalendarEngineRole {

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Requirement
  protected Execution execution;

  @Override
  public long countEvents(ICalendar cal) {
    long count = 0;
    String key = getCalCountCacheKey(cal);
    Object cachedCount = execution.getContext().getProperty(key);
    if (cachedCount != null) {
      count = (Long) cachedCount;
      getLogger().debug("countEvents: from cache count {} for key '{}'", count, key);
    } else {
      count = countEventsInternal(cal);
      if (count > 0) {
        execution.getContext().setProperty(key, count);
        getLogger().debug("countEvents: to cache count {} for key '{}'", count, key);
      }
    }
    getLogger().debug("countEvents: got {} for cal '{}'", count, cal);
    return count;
  }

  String getCalCountCacheKey(ICalendar cal) {
    return "CalendarEngine.countEvents|" + getName() + "|" + cal.hashCode();
  }

  protected abstract long countEventsInternal(ICalendar cal);

  protected abstract Logger getLogger();

}
