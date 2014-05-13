/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.calendar;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class CalendarTest extends AbstractBridgedComponentTestCase{

  private boolean isArchiv = false;
  private Calendar cal;
  private ArrayList<EventApi> eventList;
  private XWikiContext context;
  private IEventManager eventMgrMock;
  private DocumentReference calDocRef;
  private XWiki xwiki;

  @Before
  public void setUp_CalendarTest() throws Exception {
    eventList = new ArrayList<EventApi>();
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    calDocRef = new DocumentReference(context.getDatabase(), "MyCalSpace", "MyCalDoc");
    cal = new Calendar(calDocRef, isArchiv);
    eventMgrMock = createMock(IEventManager.class);
    cal.inject_getEventCmd(eventMgrMock);
  }

  @Test
  @Deprecated
  public void testNewCalendar_deprecated_constructor() {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    cal = new Calendar(calDoc, isArchiv, context);
    assertEquals(calDocRef, cal.getDocumentReference());
  }

  @Test
  public void testNewCalendar() {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    cal = new Calendar(calDocRef, isArchiv);
    assertEquals(calDocRef, cal.getDocumentReference());
    assertEquals(isArchiv, cal.isArchive());
  }

  @Test
  public void testGetAllEvents_informationHidingSecurity() throws XWikiException {
    List<EventApi> list = Collections.emptyList();
    expect(eventMgrMock.getEvents(same(cal), eq(0), eq(0))).andReturn(list).once();
    replayAll();
    List<EventApi> events = cal.getAllEvents();
    verifyAll();
    assertNotSame("getAllEvents may not leak internal data references.",
        eventList, events);
  }

  @Test
  public void testGetAllEvents() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event2 = createMock(IEvent.class);
    event2.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event3 = createMock(IEvent.class);
    event3.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event4 = createMock(IEvent.class);
    event4.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = new Calendar(cal2DocRef, isArchiv);
    cal2.inject_getEventCmd(eventMgrMock);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(0))).andReturn(eventList);
    replayAll(event, event2, event3, event4);
    eventList.add(new EventApi(event, context));
    eventList.add(new EventApi(event2, context));
    eventList.add(new EventApi(event3, context));
    eventList.add(new EventApi(event4, context));
    List<EventApi> events = cal2.getAllEvents();
    verifyAll(event, event2, event3, event4);
    assertEquals("expecting complete eventList", eventList, events);
  }

  @Test
  public void testGetEvents_overEnd() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = new Calendar(cal2DocRef, isArchiv);
    cal2.inject_getEventCmd(eventMgrMock);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(10))).andReturn(eventList);
    replayAll(event);
    eventList.add(new EventApi(event, context));
    int start = 0;
    int nb = 10;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyAll(event);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetEvents_illegalStartValue() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = new Calendar(cal2DocRef, isArchiv);
    cal2.inject_getEventCmd(eventMgrMock);
    expect(eventMgrMock.getEvents(same(cal2), eq(5), eq(1))).andReturn(eventList);
    replayAll(event);
    eventList.add(new EventApi(event, context));
    int start = 5;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyAll(event);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetEvents_minusOneStart() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    replay(event);
    eventList.add(new EventApi(event, context));
    DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = new Calendar(cal2DocRef, isArchiv);
    cal2.inject_getEventCmd(eventMgrMock);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(1))).andReturn(eventList);
    replayAll();
    int start = -1;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyAll(event);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }
  
  @Test
  public void testSearchEvents() {
    IEventSearchQuery query = new DefaultEventSearchQuery(getContext().getDatabase());
    EventSearchResult result = createMock(EventSearchResult.class);
    
    expect(eventMgrMock.searchEvents(same(cal), same(query))).andReturn(result).once();
    
    replayAll(result);
    EventSearchResult ret = cal.searchEvents(query);
    verifyAll(result);
    
    assertSame(result, ret);
  }

  @Test
  public void testGetNrOfEvents_emptyList() {
    expect(eventMgrMock.countEvents(eq(cal))).andReturn(0L);
    replayAll();
    assertEquals("must be zero for empty list.", 0, cal.getNrOfEvents());
    verifyAll();
  }

  @Test
  public void testGetNrOfEvents_nonEmptyList() throws XWikiException {
    ArrayList<Long> eventList = new ArrayList<Long>();
    eventList.add(123l);
    DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = new Calendar(cal2DocRef, isArchiv);
    cal2.inject_getEventCmd(eventMgrMock);
    expect(eventMgrMock.countEvents(eq(cal2))).andReturn(123l);
    replayAll();
    long numEvents = cal2.getNrOfEvents();
    verifyAll();
    assertEquals("Expecting size of eventList.", 123l, numEvents);
  }

  @Test
  public void testIsArchive() {
    assertEquals("isArchive must return the isArchive value given in "
        + "the constructor call", isArchiv, cal.isArchive());
  }

  @Test
  public void testGetDocumentReference() {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    cal = new Calendar(calDocRef, isArchiv);
    assertEquals(calDocRef, cal.getDocumentReference());
  }

  @Test
  public void testGetStartDate() {
    Date startDateBefore = new Date();
    cal = new Calendar(calDocRef, isArchiv);
    Date startDateAfter = new Date();
    assertTrue(startDateBefore.compareTo(cal.getStartDate()) <= 0);
    assertTrue(startDateAfter.compareTo(cal.getStartDate()) >= 0);
  }

  @Test
  public void testSetTimestamp() {
    Date startDate = cal.getStartDate();
    Date newStartDate = new Date();
    cal.setStartTimestamp(newStartDate);
    assertNotNull(cal.getStartDate());
    assertNotSame(startDate, cal.getStartDate());
    assertEquals(newStartDate, cal.getStartDate());
  }

  @Test
  public void testSetStartDate() {
    Date startDate = cal.getStartDate();
    Date newStartDate = new Date();
    cal.setStartDate(newStartDate);
    assertNotNull(cal.getStartDate());
    assertNotSame(startDate, cal.getStartDate());
    assertEquals(getCalService().getMidnightDate(newStartDate), cal.getStartDate());
  }

  @Test
  public void testSetStartDate_null() {
    Date startDate = cal.getStartDate();
    cal.setStartDate(null);
    assertNotNull(cal.getStartDate());
    assertSame(startDate, cal.getStartDate());
  }

  @Test
  public void testGetAllowedSpaces() throws Exception {
    ICalendarService calServiceMock = createMock(ICalendarService.class);
    cal.injectCalService(calServiceMock);
    String eventSpace1 = "calEventSpace1";
    String eventSpace2 = "calEventSpace2";
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(Arrays.asList(
        new String[] {eventSpace1, eventSpace2}));
    replayAll(calServiceMock);
    List<String> allowedSpaces = cal.getAllowedSpaces();
    assertEquals(2, allowedSpaces.size());
    assertEquals(eventSpace1, allowedSpaces.get(0));
    assertEquals(eventSpace2, allowedSpaces.get(1));
    verifyAll(calServiceMock);
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, eventMgrMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, eventMgrMock);
    verify(mocks);
  }

}
