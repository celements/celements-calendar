package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.observation.event.CalendarCreatedEvent;
import com.celements.calendar.observation.event.CalendarCreatingEvent;
import com.celements.calendar.observation.event.CalendarDeletedEvent;
import com.celements.calendar.observation.event.CalendarDeletingEvent;
import com.celements.calendar.observation.event.CalendarUpdatedEvent;
import com.celements.calendar.observation.event.CalendarUpdatingEvent;
import com.celements.common.observation.listener.AbstractDocumentUpdateListener;

@Component(CalendarUpdateListener.NAME)
public class CalendarUpdateListener extends AbstractDocumentUpdateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CalendarUpdateListener.class);

  public static final String NAME = "CalendarUpdateListener";

  @Requirement
  private ICalendarClassConfig classConf;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return classConf.getCalendarClassRef(wikiRef);
  }

  @Override
  protected Event getCreatingEvent(DocumentReference docRef) {
    return new CalendarCreatingEvent();
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new CalendarCreatedEvent();
  }

  @Override
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return new CalendarUpdatingEvent();
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return new CalendarUpdatedEvent();
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return new CalendarDeletingEvent();
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new CalendarDeletedEvent();
  }

  @Override
  protected boolean includeDocFields() {
    return false;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
