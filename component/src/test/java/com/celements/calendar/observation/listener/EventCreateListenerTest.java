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
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class EventCreateListenerTest extends AbstractComponentTest {

  private EventCreateListener listener;

  @Before
  public void setUp_EventCreateListenerTest() throws Exception {
    listener = (EventCreateListener) Utils.getComponent(EventListener.class, 
        EventCreateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("EventCreateListener", listener.getName());
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
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
