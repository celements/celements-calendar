package com.celements.calendar;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;

@ComponentRole
public class CalendarClassConfig implements ICalendarClassConfig {

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  public DocumentReference getCalendarClassRef() {
    return getCalendarClassRef(null);
  }

  @Override
  public DocumentReference getCalendarClassRef(WikiReference wikiRef) {
    return new DocumentReference(CALENDAR_CONFIG_CLASS_DOC, getSpaceRef(wikiRef));
  }

  @Override
  public DocumentReference getCalendarEventClassRef() {
    return getCalendarEventClassRef(null);
  }

  @Override
  public DocumentReference getCalendarEventClassRef(WikiReference wikiRef) {
    return new DocumentReference(CALENDAR_EVENT_CLASS_DOC, getSpaceRef(wikiRef));
  }

  @Override
  public DocumentReference getSubscriptionClassRef() {
    return getSubscriptionClassRef(null);
  }

  @Override
  public DocumentReference getSubscriptionClassRef(WikiReference wikiRef) {
    return new DocumentReference(SUBSCRIPTION_CLASS_DOC, getSpaceRef(wikiRef));
  }

  private SpaceReference getSpaceRef(WikiReference wikiRef) {
    return webUtilsService.resolveSpaceReference(CALENDAR_SPACE, wikiRef);
  }

}
