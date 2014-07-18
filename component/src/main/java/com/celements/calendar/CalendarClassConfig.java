package com.celements.calendar;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public class CalendarClassConfig implements ICalendarClassConfig {

  public DocumentReference getCalendarClassRef(String wikiName) {
    return new DocumentReference(wikiName, CALENDAR_CONFIG_CLASS_SPACE,
        CALENDAR_CONFIG_CLASS_DOC);
  }

  public DocumentReference getCalendarEventClassRef(String wikiName) {
    return new DocumentReference(wikiName, CALENDAR_EVENT_CLASS_SPACE,
        CALENDAR_EVENT_CLASS_DOC);
  }

  public DocumentReference getSubscriptionClassRef(String wikiName) {
    return new DocumentReference(wikiName, SUBSCRIPTION_CLASS_SPACE,
        SUBSCRIPTION_CLASS_DOC);
  }

}
