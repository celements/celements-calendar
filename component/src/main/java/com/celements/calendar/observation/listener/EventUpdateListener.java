package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.observation.event.EventCreatedEvent;
import com.celements.calendar.observation.event.EventCreatingEvent;
import com.celements.calendar.observation.event.EventDeletedEvent;
import com.celements.calendar.observation.event.EventDeletingEvent;
import com.celements.calendar.observation.event.EventUpdatedEvent;
import com.celements.calendar.observation.event.EventUpdatingEvent;
import com.celements.common.observation.listener.AbstractDocumentUpdateListener;

@Component(EventUpdateListener.NAME)
public class EventUpdateListener extends AbstractDocumentUpdateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventUpdateListener.class);

  public static final String NAME = "EventUpdateListener";

  @Requirement
  private ICalendarClassConfig classConf;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return classConf.getCalendarEventClassRef(wikiRef);
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
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return new EventUpdatingEvent();
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return new EventUpdatedEvent();
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
  protected boolean includeDocFields() {
    return true;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
