package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.CalendarCreatedEvent;
import com.celements.calendar.observation.event.CalendarCreatingEvent;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.observation.listener.AbstractDocumentCreateListener;

@Component(CalendarCreateListener.NAME)
public class CalendarCreateListener extends AbstractDocumentCreateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CalendarCreateListener.class);

  public static final String NAME = "CalendarCreateListener";

  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calendarClasses;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return ((CalendarClasses) calendarClasses).getCalendarClassRef(wikiRef.getName());
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
  protected Logger getLogger() {
    return LOGGER;
  }

}
