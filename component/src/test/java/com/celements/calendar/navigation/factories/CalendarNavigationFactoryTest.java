package com.celements.calendar.navigation.factories;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.IEvent;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationFactoryTest extends AbstractBridgedComponentTestCase {

  private CalendarNavigationFactory calNavFactory;
  private XWikiContext context;
  private XWiki xwiki;
  private IEventManager eventMgrMock;

  @Before
  public void setUp_CalendarNavigationFactoryTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    calNavFactory = new CalendarNavigationFactory(context);
    eventMgrMock = createMockAndAddToDefault(IEventManager.class);
    calNavFactory.injectEventMgr(eventMgrMock);
  }

  @Test
  public void testInjectEventMgr_injected() {
    replayDefault();
    IEventManager eventMgr = calNavFactory.getEventMgr();
    assertNotNull(eventMgr);
    assertSame(eventMgr, calNavFactory.getEventMgr());
    assertSame(eventMgr, eventMgrMock);
    assertNotSame(eventMgr, Utils.getComponent(IEventManager.class));
    verifyDefault();
  }

  @Test
  public void testGetEventMgr_notInjected() {
    calNavFactory.injectEventMgr(null);
    replayDefault();
    IEventManager eventMgr = calNavFactory.getEventMgr();
    assertNotNull(eventMgr);
    assertSame(eventMgr, calNavFactory.getEventMgr());
    assertSame(eventMgr, Utils.getComponent(IEventManager.class));
    assertNotSame(eventMgr, eventMgrMock);
    verifyDefault();
  }

  @Test
  public void testGetStartNavDetails_DocRef_nullStartDate() throws Exception {
    expect(xwiki.getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("hql"), same(context))).andReturn("lucene").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("myCollection"), eq(""),
        same(context))).andReturn("de").anyTimes();
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "progon",
        "myCollection");
    Capture<ICalendar> calCapture = new Capture<ICalendar>();
    expect(eventMgrMock.countEvents(capture(calCapture))).andReturn(5L).once();
    Capture<ICalendar> calCapture2 = new Capture<ICalendar>();
    IEvent mockEvent = createMockAndAddToDefault(IEvent.class);
    expect(eventMgrMock.getFirstEvent(capture(calCapture2))).andReturn(mockEvent).once();
    expect(mockEvent.getEventDate()).andReturn(null).anyTimes();
    replayDefault();
    NavigationDetails startNavDetails = calNavFactory.getStartNavDetails(calDocRef);
    assertNotNull(startNavDetails);
    assertEquals(ICalendarClassConfig.DATE_LOW, startNavDetails.getStartDate());
    assertEquals(0, startNavDetails.getOffset());
    assertSame(calCapture.getValue(), calCapture2.getValue());
    verifyDefault();
  }

  @Test
  public void testGetStartNavDetails_EventSearchResult_nullStartDate() throws Exception {
    expect(xwiki.getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("hql"), same(context))).andReturn("lucene").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("myCollection"), eq(""),
        same(context))).andReturn("de").anyTimes();
    EventSearchResult mockSearchResult = createMockAndAddToDefault(
        EventSearchResult.class);
    expect(mockSearchResult.getSize()).andReturn(5).anyTimes();
    IEvent mockEvent = createMockAndAddToDefault(IEvent.class);
    expect(mockEvent.getEventDate()).andReturn(null).anyTimes();
    List<IEvent> resultList = Arrays.asList(mockEvent);
    expect(mockSearchResult.getEventList(eq(0), eq(1))).andReturn(resultList);
    replayDefault();
    NavigationDetails startNavDetails = calNavFactory.getStartNavDetails(
        mockSearchResult);
    assertNotNull(startNavDetails);
    assertEquals(ICalendarClassConfig.DATE_LOW, startNavDetails.getStartDate());
    assertEquals(0, startNavDetails.getOffset());
    verifyDefault();
  }

  @Test
  public void testGetStartNavDetails_emptyCalendar_NPE() {
    EventSearchResult mockSearchResult = createMockAndAddToDefault(
        EventSearchResult.class);
    expect(mockSearchResult.getSize()).andReturn(0).anyTimes();
    replayDefault();
    try {
      calNavFactory.getStartNavDetails(mockSearchResult);
      fail("Expecting EmptyCalendarListException for empty calendar");
    } catch(EmptyCalendarListException eCalListExp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetEndNavDetails_emptyCalendar_NPE() throws Exception {
    expect(xwiki.getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("hql"), same(context))).andReturn("lucene").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de").anyTimes();
    DocumentReference calDocRef = new DocumentReference("db", "progon", "myCollection");
    EventSearchResult mockSearchResult = createMockAndAddToDefault(
        EventSearchResult.class);
    IEventSearchQuery query = new DefaultEventSearchQuery("db");
    expect(eventMgrMock.searchEvents(isA(ICalendar.class), same(query))).andReturn(
        mockSearchResult).atLeastOnce();
    expect(mockSearchResult.getSize()).andReturn(0).anyTimes();
    replayDefault();
    try {
      calNavFactory.getEndNavDetails(calDocRef, 10, query);
      fail("Expecting EmptyCalendarListException for empty calendar");
    } catch(EmptyCalendarListException eCalListExp) {
      //expected
    }
    verifyDefault();
  }

}
