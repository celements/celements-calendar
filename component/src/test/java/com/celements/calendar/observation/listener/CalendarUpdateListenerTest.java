package com.celements.calendar.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.CalendarCreatedEvent;
import com.celements.calendar.observation.event.CalendarCreatingEvent;
import com.celements.calendar.observation.event.CalendarDeletedEvent;
import com.celements.calendar.observation.event.CalendarDeletingEvent;
import com.celements.calendar.observation.event.CalendarUpdatedEvent;
import com.celements.calendar.observation.event.CalendarUpdatingEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class CalendarUpdateListenerTest extends AbstractBridgedComponentTestCase {

  private CalendarUpdateListener listener;

  @Before
  public void setUp_CalendarUpdateListenerTest() throws Exception {
    listener = (CalendarUpdateListener) Utils.getComponent(EventListener.class, 
        CalendarUpdateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("CalendarUpdateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, 
        CalendarClasses.CALENDAR_CONFIG_CLASS_SPACE, 
        CalendarClasses.CALENDAR_CONFIG_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
  }

  @Test
  public void testCreatingEvent() {
    Event event = listener.getCreatingEvent(null);
    assertNotNull(event);
    assertSame(CalendarCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(null), event);
  }

  @Test
  public void testCreatedEvent() {
    Event event = listener.getCreatedEvent(null);
    assertNotNull(event);
    assertSame(CalendarCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(null), event);
  }

  @Test
  public void testUpdatingEvent() {
    Event event = listener.getUpdatingEvent(null);
    assertNotNull(event);
    assertSame(CalendarUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(null), event);
  }

  @Test
  public void testUpdatedEvent() {
    Event event = listener.getUpdatedEvent(null);
    assertNotNull(event);
    assertSame(CalendarUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(null), event);
  }

  @Test
  public void testDeletingEvent() {
    Event event = listener.getDeletingEvent(null);
    assertNotNull(event);
    assertSame(CalendarDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(null), event);
  }

  @Test
  public void testDeletedEvent() {
    Event event = listener.getDeletedEvent(null);
    assertNotNull(event);
    assertSame(CalendarDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new CalendarDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(null), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
