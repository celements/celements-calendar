package com.celements.calendar.navigation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.ICalendarNavigationFactory;
import com.celements.calendar.navigation.factories.INavigationDetailsFactory;
import com.celements.calendar.search.EventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class CalendarNavigationServiceTest extends AbstractBridgedComponentTestCase {

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
  public void testGetNavigationDetails_date() {
    Date startDate = new Date();
    int offset = 10;
    expect(navDetailsFactoryMock.getNavigationDetails(eq(startDate), eq(offset))
        ).andReturn(null).once();
    replayAll();
    assertNull(calNavService.getNavigationDetails(startDate, offset));
    verifyAll();
  }

  @Test
  public void testGetNavigationDetails() {
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
  public void testGetNavigationDetails_query() {
    DocumentReference calDocRef = new DocumentReference("myWiki", "mySpace", "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference("myWiki", "mySpace", "myEvent");
    IEvent event = new Event(eventDocRef);
    EventSearchQuery query = new EventSearchQuery(new Date(), new Date(), "");
    expect(navDetailsFactoryMock.getNavigationDetails(calDocRef, event, query)
        ).andReturn(null).once();
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
