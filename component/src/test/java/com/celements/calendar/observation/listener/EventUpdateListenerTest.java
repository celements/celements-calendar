package com.celements.calendar.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.EventCreatedEvent;
import com.celements.calendar.observation.event.EventCreatingEvent;
import com.celements.calendar.observation.event.EventDeletedEvent;
import com.celements.calendar.observation.event.EventDeletingEvent;
import com.celements.calendar.observation.event.EventUpdatedEvent;
import com.celements.calendar.observation.event.EventUpdatingEvent;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class EventUpdateListenerTest extends AbstractComponentTest {

  private EventUpdateListener listener;

  @Before
  public void setUp_EventUpdateListenerTest() throws Exception {
    listener = (EventUpdateListener) Utils.getComponent(EventListener.class, 
        EventUpdateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("EventUpdateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    String wikiName = "myWiki";
    DocumentReference classRef = new DocumentReference(wikiName, 
        CalendarClasses.CALENDAR_EVENT_CLASS_SPACE, 
        CalendarClasses.CALENDAR_EVENT_CLASS_DOC);
    assertEquals(classRef, listener.getRequiredObjClassRef(new WikiReference(wikiName)));
  }

  @Test
  public void testCreatingEvent() {
    Event event = listener.getCreatingEvent(null);
    assertNotNull(event);
    assertSame(EventCreatingEvent.class, event.getClass());
    assertTrue(event.matches(new EventCreatingEvent()));
    assertNotSame(listener.getCreatingEvent(null), event);
  }

  @Test
  public void testCreatedEvent() {
    Event event = listener.getCreatedEvent(null);
    assertNotNull(event);
    assertSame(EventCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new EventCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(null), event);
  }

  @Test
  public void testUpdatingEvent() {
    Event event = listener.getUpdatingEvent(null);
    assertNotNull(event);
    assertSame(EventUpdatingEvent.class, event.getClass());
    assertTrue(event.matches(new EventUpdatingEvent()));
    assertNotSame(listener.getUpdatingEvent(null), event);
  }

  @Test
  public void testUpdatedEvent() {
    Event event = listener.getUpdatedEvent(null);
    assertNotNull(event);
    assertSame(EventUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new EventUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(null), event);
  }

  @Test
  public void testDeletingEvent() {
    Event event = listener.getDeletingEvent(null);
    assertNotNull(event);
    assertSame(EventDeletingEvent.class, event.getClass());
    assertTrue(event.matches(new EventDeletingEvent()));
    assertNotSame(listener.getDeletingEvent(null), event);
  }

  @Test
  public void testDeletedEvent() {
    Event event = listener.getDeletedEvent(null);
    assertNotNull(event);
    assertSame(EventDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new EventDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(null), event);
  }

  @Test
  public void testIncludeDocFields() {
    assertTrue(listener.includeDocFields());
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
