package com.celements.calendar.navigation.factories;

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
import com.celements.calendar.search.EventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NavigationDetailsFactoryTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private NavigationDetailsFactory navDetailsFactory;
  private IEventManager eventMgrMock;
  private IEvent eventMock;

  @Before
  public void setUp_EventsManagerTest() {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    navDetailsFactory = new NavigationDetailsFactory();
    eventMgrMock = createMock(IEventManager.class);
    navDetailsFactory.injectEventMgr(eventMgrMock);
    eventMock = createMock(IEvent.class);
  }

  @Test
  public void testGetNavigationDetails_startDate_offset() {
    Date startDate = new Date();
    int offset = 20;
    NavigationDetails navDetails = navDetailsFactory.getNavigationDetails(startDate,
        offset);
    assertEquals(startDate, navDetails.getStartDate());
    assertEquals(offset, navDetails.getOffset());
  }

  @Test
  public void testGetNavigationDetails_noStartDate() throws XWikiException {
    DocumentReference cal1Ref = new DocumentReference("myWiki", "mySpace", "cal1");
    DocumentReference eventRef = new DocumentReference("myWiki", "myEventSpace", "Event1");
    XWikiDocument cal1Doc = new XWikiDocument(cal1Ref);

    expect(eventMock.getEventDate()).andReturn(null).atLeastOnce();
    expect(eventMock.getDocumentReference()).andReturn(eventRef).anyTimes();
    expect(xwiki.getDocument(eq(cal1Ref), same(context))).andReturn(cal1Doc).anyTimes();

    replayAll();
    assertNull(navDetailsFactory.getNavigationDetails(cal1Ref, eventMock));
    verifyAll();
  }

  @Test
  public void testGetNavigationDetails() throws XWikiException {
    List<String> spaces = Arrays.asList("asdf");
    EntityReference calSpace = new SpaceReference(spaces.get(0), new WikiReference(
        "myWiki"));
    Date eventDate = new Date();
    Date eventMidnightDate = getCalService().getMidnightDate(eventDate);
    DocumentReference eventDocRef = new DocumentReference("myWiki", "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event2 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent2"));
    IEvent event3 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent3"));
    IEvent event4 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent4"));
    List<IEvent> eventList = Arrays.asList(event2, event3, eventMock, event4);
    DocumentReference calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    String lang = "de";

    expect(eventMock.getEventDate()).andReturn(eventDate).once();

    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(eventMgrMock.getEventsInternal((ICalendar) anyObject(), eq(0), eq(10))
        ).andReturn(eventList).once();

    replayAll();
    NavigationDetails navDetails = navDetailsFactory.getNavigationDetails(calDocRef,
        eventMock);
    verifyAll();

    assertNotNull(navDetails);
    assertEquals(eventMidnightDate, navDetails.getStartDate());
    assertEquals(2, navDetails.getOffset());
  }

  @Test
  public void testGetNavigationDetails_query() throws XWikiException {
    List<String> spaces = Arrays.asList("asdf");
    EntityReference calSpace = new SpaceReference(spaces.get(0), new WikiReference(
        "myWiki"));
    Date eventDate = new Date();
    Date eventMidnightDate = getCalService().getMidnightDate(eventDate);
    DocumentReference eventDocRef = new DocumentReference("myWiki", "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event2 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent2"));
    IEvent event3 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent3"));
    IEvent event4 = new Event(new DocumentReference("myWiki", "mySpace", "myEvent4"));
    List<IEvent> eventList = Arrays.asList(event2, event3, eventMock, event4);
    DocumentReference calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    String lang = "de";
    EventSearchQuery query = new EventSearchQuery(new Date(0), new Date(), "some term");
    EventSearchResult searchResultMock = createMock(EventSearchResult.class);

    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(eventMgrMock.searchEvents((ICalendar) anyObject(), eq(query))).andReturn(
        searchResultMock).once();
    expect(searchResultMock.getEventList(eq(0), eq(10))).andReturn(eventList).once();

    replayAll(searchResultMock);
    NavigationDetails navDetails = navDetailsFactory.getNavigationDetails(calDocRef,
        eventMock, query);
    verifyAll(searchResultMock);

    assertNotNull(navDetails);
    assertEquals(eventMidnightDate, navDetails.getStartDate());
    assertEquals(2, navDetails.getOffset());
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

  private void replayAll(Object ... mocks) {
    replay(eventMgrMock, eventMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(eventMgrMock, eventMock);
    verify(mocks);
  }

}
