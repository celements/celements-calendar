package com.celements.calendar.observation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.observation.event.CalendarDeletedEvent;
import com.celements.calendar.observation.event.CalendarDeletingEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class CalendarDeleteListenerTest extends AbstractBridgedComponentTestCase {

  private CalendarDeleteListener listener;

  @Before
  public void setUp_CalendarDeleteListenerTest() throws Exception {
    listener = (CalendarDeleteListener) Utils.getComponent(EventListener.class, 
        CalendarDeleteListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("CalendarDeleteListener", listener.getName());
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
