package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.EventCreatedEvent;
import com.celements.calendar.observation.event.EventCreatingEvent;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentCreateListener;

@Component(EventCreateListener.NAME)
public class EventCreateListener extends AbstractDocumentCreateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventCreateListener.class);

  public static final String NAME = "EventCreateListener";

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
  protected Event getCreatingEvent(DocumentReference docRef) {
    return new EventCreatingEvent();
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new EventCreatedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
