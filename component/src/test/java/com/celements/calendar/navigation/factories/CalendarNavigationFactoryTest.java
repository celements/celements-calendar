package com.celements.calendar.navigation.factories;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractComponentTest;

public class CalendarNavigationFactoryTest extends AbstractComponentTest {

  private CalendarNavigationFactory calNavFactory;
  private ICalendarService calServiceMock;
  private ICalendar calMock;

  private DocumentReference calDocRef;

  @Before
  public void setUp_CalendarNavigationFactoryTest() throws Exception {
    calNavFactory = new CalendarNavigationFactory();
    calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    calNavFactory.injectCalService(calServiceMock);
    calMock = createMockAndAddToDefault(ICalendar.class);
    calDocRef = new DocumentReference("db", "space", "calDoc");
    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
  }

  @Test
  public void testGetStartNavDetails() throws Exception {
    Date startDate = new Date();
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(ICalendarClassConfig.DATE_LOW)))
        .andReturn(calMock).once();
    IEvent evMock = createMockAndAddToDefault(IEvent.class);
    expect(calMock.getFirstEvent()).andReturn(evMock).once();
    expect(evMock.getEventDate()).andReturn(startDate).once();

    replayDefault();
    NavigationDetails ret = calNavFactory.getStartNavDetails(calDocRef);
    verifyDefault();

    assertEquals(startDate, ret.getStartDate());
    assertEquals(0, ret.getOffset());
  }

  @Test
  public void testGetStartNavDetails_nullStartDate() throws Exception {
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(ICalendarClassConfig.DATE_LOW)))
        .andReturn(calMock).once();
    IEvent evMock = createMockAndAddToDefault(IEvent.class);
    expect(calMock.getFirstEvent()).andReturn(evMock).once();
    expect(evMock.getEventDate()).andReturn(null).once();

    replayDefault();
    NavigationDetails ret = calNavFactory.getStartNavDetails(calDocRef);
    verifyDefault();

    assertEquals(ICalendarClassConfig.DATE_LOW, ret.getStartDate());
    assertEquals(0, ret.getOffset());
  }

  @Test
  public void testGetStartNavDetails_NDE_nullEvent() throws Exception {
    expect(calServiceMock.getCalendar(eq(calDocRef), eq(ICalendarClassConfig.DATE_LOW)))
        .andReturn(calMock).once();
    expect(calMock.getFirstEvent()).andReturn(null).once();

    replayDefault();
    try {
      calNavFactory.getStartNavDetails(calDocRef);
      fail("expecting NavigationDetailException");
    } catch (NavigationDetailException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetStartNavDetails_EventSearchResult_nullStartDate() throws Exception {
    EventSearchResult resultMock = createMockAndAddToDefault(EventSearchResult.class);
    expect(resultMock.getSize()).andReturn(5).anyTimes();
    IEvent mockEvent = createMockAndAddToDefault(IEvent.class);
    expect(mockEvent.getEventDate()).andReturn(null).once();
    List<IEvent> resultList = Arrays.asList(mockEvent);
    expect(resultMock.getEventList(eq(0), eq(1))).andReturn(resultList).once();
    replayDefault();
    NavigationDetails startNavDetails = calNavFactory.getStartNavDetails(resultMock);
    assertNotNull(startNavDetails);
    assertEquals(ICalendarClassConfig.DATE_LOW, startNavDetails.getStartDate());
    assertEquals(0, startNavDetails.getOffset());
    verifyDefault();
  }

  @Test
  public void testGetStartNavDetails_emptyCalendar_NPE() throws Exception {
    EventSearchResult mockSearchResult = createMockAndAddToDefault(
        EventSearchResult.class);
    expect(mockSearchResult.getSize()).andReturn(0).anyTimes();
    replayDefault();
    try {
      calNavFactory.getStartNavDetails(mockSearchResult);
      fail("Expecting NavigationDetailException for empty calendar");
    } catch (NavigationDetailException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetEndNavDetails_NDE_emptyCalendar() throws Exception {
    expect(calServiceMock.getCalendarArchive(eq(calDocRef),
        eq(ICalendarClassConfig.DATE_HIGH))).andReturn(calMock).once();
    EventSearchResult resultMock = createMockAndAddToDefault(EventSearchResult.class);
    IEventSearchQuery query = new DefaultEventSearchQuery(new WikiReference("db"));
    expect(calMock.searchEvents(same(query))).andReturn(resultMock).atLeastOnce();
    expect(resultMock.getSize()).andReturn(0).anyTimes();
    replayDefault();
    try {
      calNavFactory.getEndNavDetails(calDocRef, 10, query);
      fail("Expecting NavigationDetailException for empty calendar");
    } catch (NavigationDetailException exc) {
      // expected
    }
    verifyDefault();
  }

}
