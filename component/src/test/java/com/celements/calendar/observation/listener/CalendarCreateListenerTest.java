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
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CalendarCreateListenerTest extends AbstractComponentTest {

  private CalendarCreateListener listener;

  @Before
  public void setUp_CalendarCreateListenerTest() throws Exception {
    listener = (CalendarCreateListener) Utils.getComponent(EventListener.class, 
        CalendarCreateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("CalendarCreateListener", listener.getName());
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
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
