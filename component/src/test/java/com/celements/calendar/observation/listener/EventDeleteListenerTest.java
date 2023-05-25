package com.celements.calendar.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.EventDeletedEvent;
import com.celements.calendar.observation.event.EventDeletingEvent;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class EventDeleteListenerTest extends AbstractComponentTest {

  private EventDeleteListener listener;

  @Before
  public void setUp_EventDeleteListenerTest() throws Exception {
    listener = (EventDeleteListener) Utils.getComponent(EventListener.class, 
        EventDeleteListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("EventDeleteListener", listener.getName());
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
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
