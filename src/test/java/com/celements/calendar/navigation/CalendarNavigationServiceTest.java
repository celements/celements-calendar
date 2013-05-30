package com.celements.calendar.navigation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CalendarNavigationService calNavService;
  private IEventManager eventMgrMock;

  @Before
  public void setUp_EventsManagerTest() {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    calNavService = (CalendarNavigationService) Utils.getComponent(
        ICalendarNavigationService.class);
    eventMgrMock = createMock(IEventManager.class);
    calNavService.injectEventManager(eventMgrMock);
  }

  @Test
  public void testGetNavigationDetails_noStartDate() throws XWikiException {
    DocumentReference cal1Ref = new DocumentReference(context.getDatabase(), "mySpace",
        "cal1");
    Event event = createMock(Event.class);
    DocumentReference eventRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    XWikiDocument cal1Doc = new XWikiDocument(cal1Ref);

    expect(event.getEventDate()).andReturn(null).atLeastOnce();
    expect(event.getDocumentReference()).andReturn(eventRef).anyTimes();
    expect(xwiki.getDocument(eq(cal1Ref), same(context))).andReturn(cal1Doc).anyTimes();

    replayAll(event);
    assertNull(calNavService.getNavigationDetails(cal1Ref, event));
    verifyAll(event);
  }

  @Test
  public void testGetNavigationDetails() throws XWikiException {
    List<String> spaces = Arrays.asList("asdf");
    EntityReference calSpace = new SpaceReference(spaces.get(0), new WikiReference(
        context.getDatabase()));
    Date eventDate = new Date();
    Date eventMidnightDate = getCalService().getMidnightDate(new Date());
    IEvent event = createMock(IEvent.class);
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event2 = new Event(new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2"));
    IEvent event3 = new Event(new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent3"));
    IEvent event4 = new Event(new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent4"));
    List<IEvent> eventList = Arrays.asList(event2, event3, event, event4);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    String lang = "de";

    expect(event.getEventDate()).andReturn(eventDate).once();

    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(eventMgrMock.getEventsInternal((ICalendar) anyObject(), eq(0), eq(10))).andReturn(eventList).once();

    replayAll(event);
    NavigationDetails navDetails = calNavService.getNavigationDetails(calDocRef, event);
    verifyAll(event);

    assertNotNull(navDetails);
    assertEquals(eventMidnightDate, navDetails.getStartDate());
    assertEquals(2, navDetails.getOffset());
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

  private void replayAll(Object ... mocks) {
    replay(eventMgrMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(eventMgrMock);
    verify(mocks);
  }

}
