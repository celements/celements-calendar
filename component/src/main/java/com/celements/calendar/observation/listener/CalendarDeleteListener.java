package com.celements.calendar.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.observation.event.CalendarDeletedEvent;
import com.celements.calendar.observation.event.CalendarDeletingEvent;
import com.celements.common.observation.listener.AbstractDocumentDeleteListener;

@Component(CalendarDeleteListener.NAME)
public class CalendarDeleteListener extends AbstractDocumentDeleteListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarDeleteListener.class);

  public static final String NAME = "CalendarDeleteListener";

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
  protected Event getDeletingEvent(DocumentReference docRef) {
    return new CalendarDeletingEvent();
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new CalendarDeletedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
