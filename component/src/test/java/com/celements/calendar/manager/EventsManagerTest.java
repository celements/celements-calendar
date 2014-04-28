package com.celements.calendar.manager;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class EventsManagerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private EventsManager eventsMgr;
  private XWiki xwiki;
  private ICalendarService calServiceMock;
  private ICalendarEngineRole engineMock;

  @Before
  public void setUp_EventsManagerTest() {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    eventsMgr = (EventsManager) Utils.getComponent(IEventManager.class);
    calServiceMock = createMock(ICalendarService.class);
    eventsMgr.injectCalService(calServiceMock);
    engineMock = createMock(ICalendarEngineRole.class);
  }

  @Test
  public void getEventsInternal() throws XWikiException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date();
    EntityReference calSpace = new SpaceReference(spaces.get(0), new WikiReference(
        context.getDatabase()));
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");

    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event = createMock(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event2 = createMock(IEvent.class);
    ICalendar calMock = createMock(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();

    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(engineMock.getEvents(eq(startDate), eq(false),
        eq(lang), eq(spaces), eq(2), eq(5))).andReturn(Arrays.asList(event, event2)
            ).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn(spaces.get(0)
        ).times(2);
    expect(event.getDocumentReference()).andReturn(eventDocRef).once();
    expect(event2.getDocumentReference()).andReturn(eventDocRef2).once();

    replayAll(calMock, event, event2);
    List<IEvent> events = eventsMgr.getEventsInternal(calMock, 2, 5);
    verifyAll(calMock, event, event2);

    assertNotNull(events);
    assertEquals(2, events.size());
    assertEquals(event, events.get(0));
    assertEquals(event2, events.get(1));
  }

  @Test
  public void testCountEvents() throws XWikiException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date();
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar calMock = createMock(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();

    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(engineMock.countEvents(eq(startDate), eq(false), eq(lang), eq(spaces))
        ).andReturn(2L).once();

    replayAll(calMock);
    long countEvent = eventsMgr.countEvents(calMock);
    verifyAll(calMock);

    assertEquals(2L, countEvent);
  }

  @Test
  public void testCountEvents_checkCache() throws XWikiException {
    Date startDate = new Date();
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar cal = new Calendar(calDocRef, false);
    cal.setStartDate(startDate);

    Execution execution = Utils.getComponent(Execution.class);
    execution.getContext().setProperty("EventsManager.countEvents|xwikidb:mySpace.myCalDoc"
        + "|false|" + getCalService().getMidnightDate(startDate).getTime(), 12345L);
    eventsMgr.injectExecution(execution);

    replayAll();
    long countEvent = eventsMgr.countEvents(cal);
    verifyAll();

    assertEquals(12345L, countEvent);
  }

  @Test
  public void testIsHomeCalendar() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
        "Event1");
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn("inbox"
        ).once();
    replayAll();
    assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
        + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
    verifyAll();
  }

  @Test
  public void testSearchEvents_dirtyLuceneWorkaraound() throws Exception {
    EventSearchQuery query = new EventSearchQuery(null, null, null);
    ICalendar calMock = createMock(ICalendar.class);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyCal");
    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    Date startDate = new Date();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    ICalendarEngineRole calEngineMock = createMock(ICalendarEngineRole.class);
    expect(calMock.getEngine()).andReturn(calEngineMock).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de");
    List<String> allowedSpaces = Arrays.asList("mySpace");
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(allowedSpaces);
    EventSearchResult mockEventSearchResults = createMock(EventSearchResult.class);
    expect(calEngineMock.searchEvents(same(query), eq(startDate), eq(false), eq("de"),
        eq(allowedSpaces))).andReturn(mockEventSearchResults);
    //!! IMPORTANT getSize MUST be called imadiatelly
    expect(mockEventSearchResults.getSize()).andReturn(10).once();
    replayAll(calMock, calEngineMock, mockEventSearchResults);
    assertSame(mockEventSearchResults, eventsMgr.searchEvents(calMock, query));
    verifyAll(calMock, calEngineMock, mockEventSearchResults);
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, calServiceMock, engineMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, calServiceMock, engineMock);
    verify(mocks);
  }

}
