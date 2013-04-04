package com.celements.calendar.manager;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class EventsManagerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private EventsManager eventsMgr;
  private XWiki xwiki;
  private ICalendarService calServiceMock;
  private IQueryService queryServiceMock;
  private IEventSearch eventSearchMock;
  private LuceneQueryApi queryMock;
  private EventSearchResult eventSearchResultMock;

  @Before
  public void setUp_EventsManagerTest() {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    eventsMgr = (EventsManager) Utils.getComponent(IEventManager.class);
    calServiceMock = createMock(ICalendarService.class);
    eventsMgr.injectCalService(calServiceMock);
    queryServiceMock = createMock(IQueryService.class);
    eventsMgr.injectQueryService(queryServiceMock);
    eventSearchMock = createMock(IEventSearch.class);
    eventsMgr.injectEventSearch(eventSearchMock);
    queryMock = createMock(LuceneQueryApi.class);
    eventSearchResultMock = createMock(EventSearchResult.class);
  }

  @Test
  public void getEventsInternal() throws XWikiException {
    String lang = "de";
    String space = "myCalSpace";
    Date startDate = new Date();
    EntityReference calSpace = new SpaceReference(space, new WikiReference(
        context.getDatabase()));
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar cal = new Calendar(calDocRef, false);
    cal.setStartDate(startDate);
    LuceneQueryRestrictionApi spaceRestriction = new LuceneQueryRestrictionApi("space",
        space);
    LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
        "de");

    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(Arrays.asList(space)
        ).once();
    expect(queryServiceMock.createRestriction("space", space)).andReturn(
        spaceRestriction).once();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang", lang)
        ).andReturn(langRestriction).once();
    expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
    expect(queryMock.addOrRestrictionList(Arrays.asList(spaceRestriction))).andReturn(
        queryMock).once();
    expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
    expect(eventSearchMock.getSearchResultFromDate(same(queryMock), eq(startDate))
        ).andReturn(eventSearchResultMock).once();

    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event = createMock(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event2 = createMock(IEvent.class);

    expect(eventSearchResultMock.getEventList(2, 5)).andReturn(Arrays.asList(event,
        event2)).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn(space
        ).times(2);
    expect(event.getDocumentReference()).andReturn(eventDocRef).times(1);
    expect(event2.getDocumentReference()).andReturn(eventDocRef2).times(1);

    replayAll(event, event2);
    List<IEvent> events = eventsMgr.getEventsInternal(cal, 2, 5);
    verifyAll(event, event2);

    assertNotNull(events);
    assertEquals(2, events.size());
    assertEquals(event, events.get(0));
    assertEquals(event2, events.get(1));
  }

  @Test
  public void getEventsInternal_isArchive() throws XWikiException {
    String lang = "de";
    String space = "myCalSpace";
    Date startDate = new Date();
    EntityReference calSpace = new SpaceReference(space, new WikiReference(
        context.getDatabase()));
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar cal = new Calendar(calDocRef, true);
    cal.setStartDate(startDate);
    LuceneQueryRestrictionApi spaceRestriction = new LuceneQueryRestrictionApi("space",
        space);
    LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
        "de");

    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(Arrays.asList(space)
        ).once();
    expect(queryServiceMock.createRestriction("space", space)).andReturn(
        spaceRestriction).once();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang", lang)
        ).andReturn(langRestriction).once();
    expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
    expect(queryMock.addOrRestrictionList(Arrays.asList(spaceRestriction))).andReturn(
        queryMock).once();
    expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
    expect(eventSearchMock.getSearchResultUptoDate(same(queryMock), eq(startDate))
        ).andReturn(eventSearchResultMock).once();

    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event = createMock(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event2 = createMock(IEvent.class);

    expect(eventSearchResultMock.getEventList(2, 5)).andReturn(Arrays.asList(event,
        event2)).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn(space
        ).times(2);
    expect(event.getDocumentReference()).andReturn(eventDocRef).times(1);
    expect(event2.getDocumentReference()).andReturn(eventDocRef2).times(1);

    replayAll(event, event2);
    List<IEvent> events = eventsMgr.getEventsInternal(cal, 2, 5);
    verifyAll(event, event2);

    assertNotNull(events);
    assertEquals(2, events.size());
    assertEquals(event, events.get(0));
    assertEquals(event2, events.get(1));
  }

  @Test
  public void testCountEvents() throws XWikiException {
    String lang = "de";
    String space = "myCalSpace";
    Date startDate = new Date();
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar cal = new Calendar(calDocRef, false);
    cal.setStartDate(startDate);
    LuceneQueryRestrictionApi spaceRestriction = new LuceneQueryRestrictionApi("space",
        space);
    LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
        "de");

    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(Arrays.asList(space)
        ).once();
    expect(queryServiceMock.createRestriction("space", space)).andReturn(
        spaceRestriction).once();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang", lang)
        ).andReturn(langRestriction).once();
    expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
    expect(queryMock.addOrRestrictionList(Arrays.asList(spaceRestriction))).andReturn(
        queryMock).once();
    expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
    expect(eventSearchMock.getSearchResultFromDate(same(queryMock), eq(startDate))
        ).andReturn(eventSearchResultMock).once();

    expect(eventSearchResultMock.getSize()).andReturn(2).once();

    replayAll();
    long countEvent = eventsMgr.countEvents(cal);
    verifyAll();

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
        + "|false|" + startDate.getTime(), 12345L);
    eventsMgr.injectExecution(execution);

    replayAll();
    long countEvent = eventsMgr.countEvents(cal);
    verifyAll();

    assertEquals(12345L, countEvent);
  }

  @Test
  public void testGetNavigationDetails_noStartDate() throws XWikiException {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference cal1Ref = new DocumentReference(context.getDatabase(), "mySpace",
        "cal1");
    Calendar cal = new Calendar(cal1Ref, false);
    Event event = createMock(Event.class);
    DocumentReference eventRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    XWikiDocument cal1Doc = new XWikiDocument(cal1Ref);

    expect(event.getEventDate()).andReturn(null).atLeastOnce();
    expect(event.getDocumentReference()).andReturn(eventRef).anyTimes();
    expect(xwiki.getDocument(eq(cal1Ref), same(context))).andReturn(cal1Doc
        ).anyTimes();

    replayAll(event);
    assertNull(eventsMgr.getNavigationDetails(event, cal));
    verifyAll(event);
  }

  @Test
  public void testGetNavigationDetails() throws XWikiException {
    String space = "asdf";
    EntityReference calSpace = new SpaceReference(space, new WikiReference(
        context.getDatabase()));
    Date eventDate = new Date();
    IEvent event = createMock(IEvent.class);
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event2 = createMock(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event3 = createMock(IEvent.class);
    DocumentReference eventDocRef3 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent3");
    eventDocRef3.setParent(calSpace);
    List<IEvent> eventList = Arrays.asList(event2, event3, event);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    Calendar cal = new Calendar(calDocRef, false);
    Date startDate = new Date(1000);
    cal.setStartDate(startDate);
    String lang = "de";
    List<String> spaces = Collections.emptyList();
    LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
        "de");

    expect(event.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang", lang)
        ).andReturn(langRestriction).once();
    expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
    expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
    expect(eventSearchMock.getSearchResultFromDate(same(queryMock), eq(eventDate))
        ).andReturn(eventSearchResultMock).once();

    expect(eventSearchResultMock.getEventList(0, 10)).andReturn(eventList).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn(space
        ).times(3);
    expect(event.getDocumentReference()).andReturn(eventDocRef).once();
    expect(event2.getDocumentReference()).andReturn(eventDocRef2).once();
    expect(event3.getDocumentReference()).andReturn(eventDocRef3).once();

    replayAll(event, event2, event3);
    NavigationDetails navDetails = eventsMgr.getNavigationDetails(event, cal);
    verifyAll(event, event2, event3);

    assertNotNull(navDetails);
    assertEquals(new NavigationDetails(eventDate, 2), navDetails);

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

  private void replayAll(Object ... mocks) {
    replay(xwiki, calServiceMock, queryServiceMock, eventSearchMock, queryMock,
        eventSearchResultMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, calServiceMock, queryServiceMock, eventSearchMock, queryMock,
        eventSearchResultMock);
    verify(mocks);
  }

}
