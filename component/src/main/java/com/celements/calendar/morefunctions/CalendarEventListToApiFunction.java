package com.celements.calendar.morefunctions;

import java.util.List;

import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

public class CalendarEventListToApiFunction implements Function<List<IEvent>, List<EventApi>> {

  @Override
  public List<EventApi> apply(List<IEvent> events) {
    return FluentIterable.from(events).filter(Predicates.notNull()).transform(
        new CalendarEventToApiFunction()).toList();
  }
}
