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
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.util.CalendarUtils;
import com.celements.calendar.util.ICalendarUtils;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class CalendarTest extends AbstractBridgedComponentTestCase{

  private boolean isArchiv = false;
  private Calendar cal;
  private ArrayList<EventApi> eventList;
  private XWikiContext context;
  private ICalendarUtils calUtils;

  @Before
  public void setUp_CalendarTest() throws Exception {
    eventList = new ArrayList<EventApi>();
    context = getContext();
    cal = new Calendar(null, isArchiv, context);
    calUtils = createMock(CalendarUtils.class);
    cal.setCalendarUtils(calUtils);
  }

  @Test
  public void testGetAllEvents_informationHidingScurity() throws XWikiException {
    List<EventApi> list = Collections.emptyList();
    expect(calUtils.getEvents((XWikiDocument)eq(null), eq(0), eq(0), eq(false),
        same(context))).andReturn(list);
    replay(calUtils);
    List<EventApi> events = cal.getAllEvents();
    verify(calUtils);
    assertNotSame("getAllEvents may not leak internal data references.",
        eventList, events);
  }

  @Test
  public void testGetAllEvents() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    eventList.add(new EventApi(event, null));
    IEvent event2 = createMock(IEvent.class);
    eventList.add(new EventApi(event2, null));
    IEvent event3 = createMock(IEvent.class);
    eventList.add(new EventApi(event3, null));
    IEvent event4 = createMock(IEvent.class);
    eventList.add(new EventApi(event4, null));
    Calendar cal2 = new Calendar(null, isArchiv, context);
    cal2.setCalendarUtils(calUtils);
    expect(calUtils.getEvents((XWikiDocument)eq(null), eq(0), eq(0), eq(false),
        same(context))).andReturn(eventList);
    replay(event, event2, event3, event4, calUtils);
    List<EventApi> events = cal2.getAllEvents();
    verify(event, event2, event3, event4, calUtils);
    assertEquals("expecting complete eventList", eventList, events);
  }

  @Test
  public void testGetEvents_overEnd() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    eventList.add(new EventApi(event, null));
    Calendar cal2 = new Calendar(null, isArchiv, context);
    cal2.setCalendarUtils(calUtils);
    expect(calUtils.getEvents((XWikiDocument)eq(null), eq(0), eq(10), eq(false),
        same(context))).andReturn(eventList);
    replay(event, calUtils);
    int start = 0;
    int nb = 10;
    List<EventApi> events = cal2.getEvents(start, nb);
    verify(event, calUtils);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetEvents_illegalStartValue() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    eventList.add(new EventApi(event, null));
    Calendar cal2 = new Calendar(null, isArchiv, context);
    cal2.setCalendarUtils(calUtils);
    expect(calUtils.getEvents((XWikiDocument)eq(null), eq(5), eq(1), eq(false),
        same(context))).andReturn(eventList);
    replay(event, calUtils);
    int start = 5;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verify(event, calUtils);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  public void testGetEvents_minusOneStart() throws XWikiException {
    ArrayList<EventApi> eventList = new ArrayList<EventApi>();
    IEvent event = createMock(IEvent.class);
    eventList.add(new EventApi(event, null));
    Calendar cal2 = new Calendar(null, isArchiv, context);
    cal2.setCalendarUtils(calUtils);
    expect(calUtils.getEvents((XWikiDocument)eq(null), eq(0), eq(1), eq(false),
        same(context))).andReturn(eventList);
    replay(event, calUtils);
    int start = -1;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verify(event, calUtils);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetNrOfEvents_emptyList() {
    assertEquals("must be zero for empty list.", 0, cal.getNrOfEvents());
  }

  @Test
  public void testGetNrOfEvents_nonEmptyList() throws XWikiException {
    ArrayList<Long> eventList = new ArrayList<Long>();
    eventList.add(123l);
    Calendar cal2 = new Calendar(null, isArchiv, context);
    cal2.setCalendarUtils(calUtils);
    expect(calUtils.countEvents((XWikiDocument)eq(null), eq(isArchiv), same(context))
        ).andReturn(123l);
    replay(calUtils);
    long numEvents = cal2.getNrOfEvents();
    verify(calUtils);
    assertEquals("Expecting size of eventList.", 123l, numEvents);
  }

  @Test
  public void testIsArchive() {
    assertEquals("isArchive must return the isArchive value given in "
        + "the constructor call", isArchiv, cal.isArchive());
  }

}
