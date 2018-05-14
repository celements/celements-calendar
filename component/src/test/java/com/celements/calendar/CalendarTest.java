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

import static com.celements.calendar.ICalendarClassConfig.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.engine.CalendarEngineHQL;
import com.celements.calendar.engine.CalendarEngineLucene;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CalendarTest extends AbstractComponentTest {

  private IEventManager eventMgrMock;
  private ICalendarService calServiceMock;
  private IModelAccessFacade modelAccessMock;

  private DocumentReference calDocRef;
  private Calendar cal;

  @Before
  public void prepareTest() throws Exception {
    eventMgrMock = registerComponentMock(IEventManager.class);
    calServiceMock = registerComponentMock(ICalendarService.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);

    calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDoc");
    cal = createCalendar(calDocRef, false);
  }

  @Test
  public void test_create() throws Exception {
    DocumentReference calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDocX");
    boolean isArchive = false;
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    addConfObj(calDoc);
    expect(modelAccessMock.getDocument(calDocRef)).andReturn(calDoc).once();

    replayDefault();
    ICalendar cal = new Calendar(calDocRef, isArchive);
    verifyDefault();

    assertEquals(calDocRef, cal.getDocumentReference());
    assertSame(calDoc, cal.getCalDoc());
    assertSame(isArchive, cal.isArchive());
  }

  @Test
  public void test_create_isArchive() throws Exception {
    DocumentReference calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDocX");
    boolean isArchive = true;
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    addConfObj(calDoc);
    expect(modelAccessMock.getDocument(calDocRef)).andReturn(calDoc).once();

    replayDefault();
    ICalendar cal = new Calendar(calDocRef, isArchive);
    verifyDefault();

    assertSame(isArchive, cal.isArchive());
  }

  @Test
  public void test_create_noConfObj() throws Exception {
    final DocumentReference calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDocX");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    expect(modelAccessMock.getDocument(calDocRef)).andReturn(calDoc).once();

    replayDefault();
    new ExceptionAsserter<CalendarCreateException>(CalendarCreateException.class) {

      @Override
      protected void execute() throws Exception {
        new Calendar(calDocRef, false);
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_create_noDoc() throws Exception {
    final DocumentReference calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDocX");
    expect(modelAccessMock.getDocument(calDocRef)).andThrow(new DocumentNotExistsException(
        calDocRef));

    replayDefault();
    new ExceptionAsserter<CalendarCreateException>(CalendarCreateException.class) {

      @Override
      protected void execute() throws Exception {
        new Calendar(calDocRef, false);
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void testGetAllEvents_informationHidingSecurity() throws Exception {
    List<EventApi> eventList = Collections.emptyList();
    expect(eventMgrMock.getEvents(same(cal), eq(0), eq(0))).andReturn(eventList).once();
    replayDefault();
    List<EventApi> events = cal.getAllEvents();
    verifyDefault();
    assertNotSame("getAllEvents may not leak internal data references.", eventList, events);
  }

  @Test
  public void testGetAllEvents() throws Exception {
    ArrayList<EventApi> eventList = new ArrayList<>();
    IEvent event = createMockAndAddToDefault(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event2 = createMockAndAddToDefault(IEvent.class);
    event2.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event3 = createMockAndAddToDefault(IEvent.class);
    event3.setLanguage(eq("de"));
    expectLastCall().once();
    IEvent event4 = createMockAndAddToDefault(IEvent.class);
    event4.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(getContext().getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = createCalendar(cal2DocRef, false);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(0))).andReturn(eventList);
    replayDefault();
    eventList.add(new EventApi(event, getContext()));
    eventList.add(new EventApi(event2, getContext()));
    eventList.add(new EventApi(event3, getContext()));
    eventList.add(new EventApi(event4, getContext()));
    List<EventApi> events = cal2.getAllEvents();
    verifyDefault();
    assertEquals("expecting complete eventList", eventList, events);
  }

  @Test
  public void testGetEvents_overEnd() throws Exception {
    ArrayList<EventApi> eventList = new ArrayList<>();
    IEvent event = createMockAndAddToDefault(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(getContext().getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = createCalendar(cal2DocRef, false);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(10))).andReturn(eventList);
    replayDefault();
    eventList.add(new EventApi(event, getContext()));
    int start = 0;
    int nb = 10;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyDefault();
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetEvents_illegalStartValue() throws Exception {
    ArrayList<EventApi> eventList = new ArrayList<>();
    IEvent event = createMockAndAddToDefault(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(getContext().getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = createCalendar(cal2DocRef, false);
    expect(eventMgrMock.getEvents(same(cal2), eq(5), eq(1))).andReturn(eventList);
    replayDefault();
    eventList.add(new EventApi(event, getContext()));
    int start = 5;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyDefault();
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testGetEvents_minusOneStart() throws Exception {
    ArrayList<EventApi> eventList = new ArrayList<>();
    IEvent event = createMockAndAddToDefault(IEvent.class);
    event.setLanguage(eq("de"));
    expectLastCall().once();
    DocumentReference cal2DocRef = new DocumentReference(getContext().getDatabase(),
        "MyCalDoc2Space", "MyCal2Doc");
    Calendar cal2 = createCalendar(cal2DocRef, false);
    expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(1))).andReturn(eventList);
    replayDefault();
    eventList.add(new EventApi(event, getContext()));
    int start = -1;
    int nb = 1;
    List<EventApi> events = cal2.getEvents(start, nb);
    verifyDefault(event);
    assertEquals("Expecting to get the full eventlist", eventList, events);
  }

  @Test
  public void testSearchEvents() throws Exception {
    IEventSearchQuery query = new DefaultEventSearchQuery(new WikiReference("myWiki"));
    EventSearchResult result = createMockAndAddToDefault(EventSearchResult.class);

    expect(eventMgrMock.searchEvents(same(cal), same(query))).andReturn(result).once();

    replayDefault();
    EventSearchResult ret = cal.searchEvents(query);
    verifyDefault();

    assertSame(result, ret);
  }

  @Test
  public void testGetNrOfEvents() throws Exception {
    long count = 123L;
    expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
    replayDefault();
    long ret = cal.getNrOfEvents();
    verifyDefault();
    assertEquals(count, ret);
  }

  @Test
  public void testIsEmpty_true() throws Exception {
    long count = 0L;
    expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
    replayDefault();
    boolean ret = cal.isEmpty();
    verifyDefault();
    assertTrue(ret);
  }

  @Test
  public void testIsEmpty_false() throws Exception {
    long count = 123L;
    expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
    replayDefault();
    boolean ret = cal.isEmpty();
    verifyDefault();
    assertFalse(ret);
  }

  @Test
  public void test_getDocumentReference() throws Exception {
    assertEquals(calDocRef, cal.getDocumentReference());
  }

  @Test
  public void testGetStartDate_newDate() throws Exception {
    java.util.Calendar gregCal = java.util.Calendar.getInstance();
    gregCal.add(java.util.Calendar.SECOND, -5);
    Date startDateBefore = gregCal.getTime();
    gregCal = java.util.Calendar.getInstance();
    gregCal.add(java.util.Calendar.SECOND, 5);
    Date startDateAfter = gregCal.getTime();
    assertTrue(startDateBefore.before(cal.getStartDate()));
    assertTrue(startDateAfter.after(cal.getStartDate()));
  }

  @Test
  public void testSetTimestamp() throws Exception {
    Date startDate = cal.getStartDate();
    Date newStartDate = new Date();
    cal.setStartTimestamp(newStartDate);
    assertNotNull(cal.getStartDate());
    assertNotSame(startDate, cal.getStartDate());
    assertEquals(newStartDate, cal.getStartDate());
  }

  @Test
  public void testSetStartDate() throws Exception {
    Date startDate = cal.getStartDate();
    Date newStartDate = new Date();
    cal.setStartDate(newStartDate);
    assertNotNull(cal.getStartDate());
    assertNotSame(startDate, cal.getStartDate());
    assertEquals(DateUtil.noTime(newStartDate), cal.getStartDate());
  }

  @Test
  public void testSetStartDate_null() throws Exception {
    Date startDate = cal.getStartDate();
    cal.setStartDate(null);
    assertNotNull(cal.getStartDate());
    assertSame(startDate, cal.getStartDate());
  }

  @Test
  public void testGetLanguage() throws Exception {
    String lang = "de";
    cal.setLanguage(lang);

    replayDefault();
    String ret = cal.getLanguage();
    verifyDefault();

    assertEquals(lang, ret);
  }

  @Test
  public void testGetLanguage_default() throws Exception {
    String lang = "de";
    SpaceReference spaceRef = new SpaceReference("evSpace", new WikiReference("db"));
    expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(spaceRef).once();
    // expect(cal.webUtilsService.getDefaultLanguage(eq(spaceRef))).andReturn(lang).once();

    replayDefault();
    String ret = cal.getLanguage();
    verifyDefault();

    assertEquals(lang, ret);
  }

  @Test
  public void test_getEventSpaceRef_emptyObject() throws Exception {
    replayDefault();
    SpaceReference spaceRef = cal.getEventSpaceRef();
    verifyDefault();

    assertEquals(calDocRef.getName(), spaceRef.getName());
    assertEquals(calDocRef.getWikiReference(), spaceRef.getParent());
  }

  @Test
  public void test_getEventSpaceRef() throws Exception {
    String eventSpaceName = "CalEventSpace";
    cal.getConfigObject().setStringValue(PROPERTY_CALENDAR_SPACE, eventSpaceName);

    replayDefault();
    SpaceReference spaceRef = cal.getEventSpaceRef();
    verifyDefault();

    assertEquals(eventSpaceName, spaceRef.getName());
    assertEquals(calDocRef.getWikiReference(), spaceRef.getParent());
  }

  @Test
  public void test_getAllowedSpaceRefs_emptyObject() throws Exception {

    replayDefault();
    List<SpaceReference> spaces = cal.getAllowedSpaceRefs();
    verifyDefault();

    assertEquals(1, spaces.size());
    assertEquals(calDocRef.getName(), spaces.get(0).getName());
    assertEquals(calDocRef.getWikiReference(), spaces.get(0).getParent());
  }

  @Test
  public void test_getAllowedSpaceRefs() throws Exception {
    String eventSpaceName = "CalEventSpace";
    cal.getConfigObject().setStringValue(PROPERTY_CALENDAR_SPACE, eventSpaceName);

    replayDefault();
    List<SpaceReference> spaces = cal.getAllowedSpaceRefs();
    verifyDefault();

    assertEquals(1, spaces.size());
    assertEquals(eventSpaceName, spaces.get(0).getName());
    assertEquals(calDocRef.getWikiReference(), spaces.get(0).getParent());
  }

  @Test
  public void test_getAllowedSpaceRefs_subscribers() throws Exception {
    String eventSpaceName = "CalEventSpace";
    cal.getConfigObject().setStringValue(PROPERTY_CALENDAR_SPACE, eventSpaceName);
    cal.getConfigObject().setStringListValue(PROPERTY_SUBSCRIBE_TO, Arrays.<String>asList(
        "mySpace.myCalDoc2", "mySpace.myCalDoc3", "mySpace.myCalDoc4"));

    Calendar cal2 = createCalendar(new DocumentReference(calDocRef.getWikiReference().getName(),
        "mySpace", "myCalDoc2"), false);
    cal2.getConfigObject().setStringValue(PROPERTY_CALENDAR_SPACE, eventSpaceName + "2");

    Calendar cal3 = createCalendar(new DocumentReference(calDocRef.getWikiReference().getName(),
        "mySpace", "myCalDoc3"), false);
    cal3.getConfigObject().setStringValue(PROPERTY_CALENDAR_SPACE, eventSpaceName + "3");

    Calendar cal4 = new Calendar(new XWikiDocument(new DocumentReference(
        calDocRef.getWikiReference().getName(), "mySpace", "myCalDoc4")), false);
    expect(modelAccessMock.getDocument(cal4.getDocumentReference())).andThrow(
        new DocumentNotExistsException(calDocRef));

    replayDefault();
    List<SpaceReference> spaces = cal.getAllowedSpaceRefs();
    verifyDefault();

    assertEquals(3, spaces.size());
    assertEquals(eventSpaceName, spaces.get(0).getName());
    assertEquals(calDocRef.getWikiReference(), spaces.get(0).getParent());
    assertEquals(eventSpaceName + "2", spaces.get(1).getName());
    assertEquals(calDocRef.getWikiReference(), spaces.get(1).getParent());
    assertEquals(eventSpaceName + "3", spaces.get(2).getName());
    assertEquals(calDocRef.getWikiReference(), spaces.get(2).getParent());
  }

  @Test
  public void testGetEngine_HQL() throws Exception {
    expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("default"), same(getContext()))).andReturn("default").once();
    replayDefault();
    ICalendarEngineRole ret = cal.getEngine();
    verifyDefault();
    assertTrue(ret instanceof CalendarEngineHQL);
  }

  @Test
  public void testGetEngine_Lucene() throws Exception {
    String hint = CalendarEngineLucene.NAME;
    expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("default"), same(getContext()))).andReturn(hint).once();

    ICalendarEngineRole engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
    DefaultComponentDescriptor<ICalendarEngineRole> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(ICalendarEngineRole.class);
    descriptor.setRoleHint(hint);
    Utils.getComponentManager().registerComponent(descriptor, engineMock);
    expect(engineMock.getName()).andReturn(hint).anyTimes();
    expect(engineMock.getEngineLimit()).andReturn(10L).once();
    expect(engineMock.countEvents(same(cal))).andReturn(5L).once();

    replayDefault();
    ICalendarEngineRole ret = cal.getEngine();
    verifyDefault();
    assertSame(engineMock, ret);
  }

  @Test
  public void testGetEngine_Lucene_overLimit() throws Exception {
    String hint = CalendarEngineLucene.NAME;
    expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), eq("calendar.engine"),
        eq("default"), same(getContext()))).andReturn(hint).once();

    ICalendarEngineRole engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
    DefaultComponentDescriptor<ICalendarEngineRole> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(ICalendarEngineRole.class);
    descriptor.setRoleHint(hint);
    Utils.getComponentManager().registerComponent(descriptor, engineMock);
    expect(engineMock.getName()).andReturn(hint).anyTimes();
    expect(engineMock.getEngineLimit()).andReturn(10L).once();
    expect(engineMock.countEvents(same(cal))).andReturn(11L).once();

    replayDefault();
    ICalendarEngineRole ret = cal.getEngine();
    verifyDefault();
    assertTrue(ret instanceof CalendarEngineHQL);
  }

  private Calendar createCalendar(DocumentReference calConfigDocRef, boolean isArchive)
      throws Exception {
    XWikiDocument calDoc = new XWikiDocument(calConfigDocRef);
    addConfObj(calDoc);
    expect(modelAccessMock.getDocument(calDoc.getDocumentReference())).andReturn(calDoc).anyTimes();
    return new Calendar(calDoc, isArchive);
  }

  private void addConfObj(XWikiDocument calDoc) {
    BaseObject calConfObj = new BaseObject();
    DocumentReference configClassRef = new DocumentReference(
        calDoc.getDocumentReference().getWikiReference().getName(), "Classes",
        "CalendarConfigClass");
    calConfObj.setXClassReference(configClassRef);
    calDoc.addXObject(calConfObj);
  }

}
