package com.celements.calendar.navigation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.ICalendarNavigationFactory;
import com.celements.calendar.navigation.factories.INavigationDetailsFactory;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationServiceTest extends AbstractComponentTest {

  private CalendarNavigationService calNavService;
  private INavigationDetailsFactory navDetailsFactoryMock;
  private ICalendarNavigationFactory calNavFactoryMock;

  @Before
  public void setUp_CalendarNavigationServiceTest() {
    calNavService = (CalendarNavigationService) Utils.getComponent(
        ICalendarNavigationService.class);
    navDetailsFactoryMock = createMock(INavigationDetailsFactory.class);
    calNavService.injectNavDetailsFactory(navDetailsFactoryMock);
    calNavFactoryMock = createMock(ICalendarNavigationFactory.class);
    calNavService.injectCalNavFactory(calNavFactoryMock);
  }

  @Test
  public void testGetNavigationDetails_date() throws Exception {
    Date startDate = new Date();
    int offset = 10;
    expect(navDetailsFactoryMock.getNavigationDetails(eq(startDate), eq(offset))
        ).andReturn(null).once();
    replayAll();
    assertNull(calNavService.getNavigationDetails(startDate, offset));
    verifyAll();
  }

  @Test
  public void testGetNavigationDetails() throws Exception {
    DocumentReference calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference("myWiki", "mySpace", "myEvent");
    IEvent event = new Event(eventDocRef);
    expect(navDetailsFactoryMock.getNavigationDetails(calDocRef, event)).andReturn(null
        ).once();
    replayAll();
    assertNull(calNavService.getNavigationDetails(calDocRef, event));
    verifyAll();
  }

  @Test
  public void testGetNavigationDetails_query() throws Exception {
    DocumentReference calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference("myWiki", "mySpace", "myEvent");
    IEvent event = new Event(eventDocRef);
    IEventSearchQuery query = new DefaultEventSearchQuery(new WikiReference("myWiki"));
    expect(navDetailsFactoryMock.getNavigationDetails(eq(calDocRef), same(event), same(
        query))).andReturn(null).once();
    replayAll();
    assertNull(calNavService.getNavigationDetails(calDocRef, event, query));
    verifyAll();
  }

  private void replayAll(Object ... mocks) {
    replay(navDetailsFactoryMock, calNavFactoryMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(navDetailsFactoryMock, calNavFactoryMock);
    verify(mocks);
  }

}
