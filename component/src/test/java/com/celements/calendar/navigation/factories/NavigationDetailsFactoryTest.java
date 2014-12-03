package com.celements.calendar.navigation.factories;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.LuceneSearchException;

public class NavigationDetailsFactoryTest extends AbstractBridgedComponentTestCase {

  private NavigationDetailsFactory navDetailsFactory;
  private ICalendarService calServiceMock;
  private ICalendar calMock;
  private IEvent eventMock;

  private DocumentReference calDocRef;
  private DocumentReference evDocRef;

  @Before
  public void setUp_NavigationDetailsFactoryTest() throws Exception {
    navDetailsFactory = new NavigationDetailsFactory();
    calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    navDetailsFactory.injectCalService(calServiceMock);
    calMock = createMockAndAddToDefault(ICalendar.class);
    calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    eventMock = createMockAndAddToDefault(IEvent.class);
    evDocRef = new DocumentReference("myWiki", "evSpace", "ev1");
    expect(eventMock.getDocumentReference()).andReturn(evDocRef).anyTimes();
  }

  @Test
  public void testGetNavigationDetails_startDate_offset() throws Exception {
    Date startDate = new Date();
    int offset = 20;
    NavigationDetails ret = navDetailsFactory.getNavigationDetails(startDate, offset);
    assertEquals(startDate, ret.getStartDate());
    assertEquals(offset, ret.getOffset());
  }

  @Test
  public void testGetNavigationDetails() throws Exception {
    Date eventDate = new Date();
    List<IEvent> eventList = Arrays.asList(eventMock);
    
    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(eventDate))).andReturn(calMock
        ).once();
    expect(calMock.getStartDate()).andReturn(eventDate).once();
    expect(calMock.getEventsInternal(eq(0), eq(10))).andReturn(eventList).once();

    replayDefault();
    NavigationDetails ret = navDetailsFactory.getNavigationDetails(calDocRef, eventMock);
    verifyDefault();

    assertEquals(eventDate, ret.getStartDate());
    assertEquals(0, ret.getOffset());
  }

  @Test
  public void testGetNavigationDetails_multiple() throws Exception {
    Date eventDate = new Date();
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class), 
        createMockAndAddToDefault(IEvent.class), 
        eventMock, 
        createMockAndAddToDefault(IEvent.class));
    
    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(eventDate))).andReturn(calMock
        ).once();
    expect(calMock.getStartDate()).andReturn(eventDate).once();
    expect(calMock.getEventsInternal(eq(0), eq(10))).andReturn(eventList).once();

    replayDefault();
    NavigationDetails ret = navDetailsFactory.getNavigationDetails(calDocRef, eventMock);
    verifyDefault();

    assertEquals(eventDate, ret.getStartDate());
    assertEquals(2, ret.getOffset());
  }

  @Test
  public void testGetNavigationDetails_query() throws Exception {
    Date eventDate = new Date();
    List<IEvent> eventList = Arrays.asList(eventMock);
    IEventSearchQuery query = new DefaultEventSearchQuery("myWiki");
    EventSearchResult searchResultMock = createMockAndAddToDefault(EventSearchResult.class);

    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(eventDate))).andReturn(calMock
        ).once();
    expect(calMock.getStartDate()).andReturn(eventDate).once();
    expect(calMock.searchEvents(same(query))).andReturn(searchResultMock).once();
    expect(searchResultMock.getEventList(eq(0), eq(10))).andReturn(eventList).once();

    replayDefault();
    NavigationDetails navDetails = navDetailsFactory.getNavigationDetails(calDocRef,
        eventMock, query);
    verifyDefault();

    assertNotNull(navDetails);
    assertEquals(eventDate, navDetails.getStartDate());
    assertEquals(0, navDetails.getOffset());
  }

  @Test
  public void testGetNavigationDetails_query_LSE() throws Exception {
    Date eventDate = new Date();
    IEventSearchQuery query = new DefaultEventSearchQuery("myWiki");
    EventSearchResult searchResultMock = createMockAndAddToDefault(EventSearchResult.class);
    Throwable cause = new LuceneSearchException();

    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(eventDate))).andReturn(calMock
        ).once();
    expect(calMock.searchEvents(same(query))).andReturn(searchResultMock).once();
    expect(searchResultMock.getEventList(eq(0), eq(10))).andThrow(cause).once();

    replayDefault();
    try {
      navDetailsFactory.getNavigationDetails(calDocRef, eventMock, query);
      fail("expecting LuceneSearchException");
    } catch (LuceneSearchException lse) {
      assertSame(cause, lse);
    }
    verifyDefault();
  }

  @Test
  public void testGetNavigationDetails_NDE_noStartDate() throws Exception {
    expect(eventMock.getEventDate()).andReturn(null).once();

    replayDefault();
    try {
      navDetailsFactory.getNavigationDetails(calDocRef, eventMock);
      fail("expecting NavigationDetailException");
    } catch (NavigationDetailException iae) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNavigationDetails_NDE_notFound() throws Exception {
    Date eventDate = new Date();
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class), 
        createMockAndAddToDefault(IEvent.class));
    
    expect(eventMock.getEventDate()).andReturn(eventDate).once();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(eventDate))).andReturn(calMock
        ).once();
    expect(calMock.getEventsInternal(eq(0), eq(10))).andReturn(eventList).once();

    replayDefault();
    try {
      navDetailsFactory.getNavigationDetails(calDocRef, eventMock);
      fail("expecting NavigationDetailException");
    } catch (NavigationDetailException iae) {
      // expected
    }
    verifyDefault();
  }

}
