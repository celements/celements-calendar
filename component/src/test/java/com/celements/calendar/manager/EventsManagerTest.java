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
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
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
    xwiki = getWikiMock();
    context = getContext();
    eventsMgr = (EventsManager) Utils.getComponent(IEventManager.class);
    calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    eventsMgr.injectCalService(calServiceMock);
    engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
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
    IEvent event = createMockAndAddToDefault(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event2 = createMockAndAddToDefault(IEvent.class);
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

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

    replayDefault();
    List<IEvent> events = eventsMgr.getEventsInternal(calMock, 2, 5);
    verifyDefault();

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
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();
    
    expect(xwiki.exists(eq(calDocRef), same(context))).andReturn(true).anyTimes();
    
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(engineMock.countEvents(eq(startDate), eq(false), eq(lang), eq(spaces))
        ).andReturn(2L).once();

    replayDefault();
    long countEvent = eventsMgr.countEvents(calMock);
    verifyDefault();

    assertEquals(2L, countEvent);
  }
  
  @Test
  public void testCountEvents_noEventExist() throws XWikiException {
    Date startDate = new Date();
    DocumentReference calDocRef = null;
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();

    replayDefault();
    long countEvent = eventsMgr.countEvents(calMock);
    verifyDefault();

    assertEquals(0, countEvent);
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

    replayDefault();
    long countEvent = eventsMgr.countEvents(cal);
    verifyDefault();

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
    replayDefault();
    assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
        + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
    verifyDefault();
  }

  @Test
  public void testSearchEvents_dirtyLuceneWorkaraound() throws Exception {
    IEventSearchQuery query = new DefaultEventSearchQuery(getContext().getDatabase());
    ICalendar calMock = createMockAndAddToDefault(ICalendar.class);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyCal");
    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    Date startDate = new Date();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    ICalendarEngineRole calEngineMock = createMockAndAddToDefault(
        ICalendarEngineRole.class);
    expect(calMock.getEngine()).andReturn(calEngineMock).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de");
    List<String> allowedSpaces = Arrays.asList("mySpace");
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(allowedSpaces);
    EventSearchResult mockEventSearchResults = createMockAndAddToDefault(
        EventSearchResult.class);
    expect(calEngineMock.searchEvents(same(query), eq(startDate), eq(false), eq("de"),
        eq(allowedSpaces))).andReturn(mockEventSearchResults).once();
    //!! IMPORTANT getSize MUST be called imadiatelly
    expect(mockEventSearchResults.getSize()).andReturn(10).once();
    replayDefault();
    assertSame(mockEventSearchResults, eventsMgr.searchEvents(calMock, query));
    verifyDefault();
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

}
