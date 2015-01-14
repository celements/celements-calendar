package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.EventDeletedEvent;
import com.celements.calendar.observation.event.EventDeletingEvent;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentDeleteListener;

@Component(EventDeleteListener.NAME)
public class EventDeleteListener extends AbstractDocumentDeleteListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDeleteListener.class);

  public static final String NAME = "EventDeleteListener";

  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calendarClasses;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return ((CalendarClasses) calendarClasses).getCalendarEventClassRef(wikiRef.getName());
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return new EventDeletingEvent();
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new EventDeletedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
