package com.celements.calendar.manager;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class EventsManagerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private EventsManager eventsMgr;
  private XWiki xwiki;
  private ICalendarService calServiceMock;
  private ICalendarEngineRole engineMock;
  
  private String db;

  @Before
  public void setUp_EventsManagerTest() {
    xwiki = getWikiMock();
    context = getContext();
    eventsMgr = (EventsManager) Utils.getComponent(IEventManager.class);
    calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    eventsMgr.injectCalService(calServiceMock);
    engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
    db = "database";
  }

  @Test
  public void getEventsInternal() throws XWikiException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date();
    EntityReference calSpace = new SpaceReference(spaces.get(0), new WikiReference(
        context.getDatabase()));
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");

    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent");
    eventDocRef.setParent(calSpace);
    IEvent event = createMockAndAddToDefault(IEvent.class);
    DocumentReference eventDocRef2 = new DocumentReference(context.getDatabase(),
        "mySpace", "myEvent2");
    eventDocRef2.setParent(calSpace);
    IEvent event2 = createMockAndAddToDefault(IEvent.class);
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();

    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(engineMock.getEvents(eq(startDate), eq(false),
        eq(lang), eq(spaces), eq(2), eq(5))).andReturn(Arrays.asList(event, event2)
            ).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn(spaces.get(0)
        ).times(2);
    expect(event.getDocumentReference()).andReturn(eventDocRef).once();
    expect(event2.getDocumentReference()).andReturn(eventDocRef2).once();

    replayDefault();
    List<IEvent> events = eventsMgr.getEventsInternal(calMock, 2, 5);
    verifyDefault();

    assertNotNull(events);
    assertEquals(2, events.size());
    assertEquals(event, events.get(0));
    assertEquals(event2, events.get(1));
  }

  @Test
  public void testCountEvents() throws XWikiException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date();
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();
    
    expect(context.getWiki().exists(eq(calDocRef), same(context))).andReturn(true).anyTimes();
    
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))
        ).andReturn(lang);
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(spaces).once();
    expect(engineMock.countEvents(eq(startDate), eq(false), eq(lang), eq(spaces))
        ).andReturn(2L).once();

    replayDefault();
    long countEvent = eventsMgr.countEvents(calMock);
    verifyDefault();

    assertEquals(2L, countEvent);
  }
  
  @Test
  public void testCountEvents_noEventExist() throws XWikiException {
    Date startDate = new Date();
    DocumentReference calDocRef = null;
    ICalendar calMock = createMockAndAddToDefault(Calendar.class);

    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.getEngine()).andReturn(engineMock).anyTimes();

    replayDefault();
    long countEvent = eventsMgr.countEvents(calMock);
    verifyDefault();

    assertEquals(0, countEvent);
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
        + "|false|" + getCalService().getMidnightDate(startDate).getTime(), 12345L);
    eventsMgr.injectExecution(execution);

    replayDefault();
    long countEvent = eventsMgr.countEvents(cal);
    verifyDefault();

    assertEquals(12345L, countEvent);
  }

  @Test
  public void testIsHomeCalendar() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
        "Event1");
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn("inbox"
        ).once();
    replayDefault();
    assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
        + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
    verifyDefault();
  }

  @Test
  public void testSearchEvents_dirtyLuceneWorkaraound() throws Exception {
    IEventSearchQuery query = new DefaultEventSearchQuery(getContext().getDatabase());
    ICalendar calMock = createMockAndAddToDefault(ICalendar.class);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyCal");
    expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
    Date startDate = new Date();
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.isArchive()).andReturn(false).anyTimes();
    ICalendarEngineRole calEngineMock = createMockAndAddToDefault(
        ICalendarEngineRole.class);
    expect(calMock.getEngine()).andReturn(calEngineMock).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(
        "de");
    List<String> allowedSpaces = Arrays.asList("mySpace");
    expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(allowedSpaces);
    EventSearchResult mockEventSearchResults = createMockAndAddToDefault(
        EventSearchResult.class);
    expect(calEngineMock.searchEvents(same(query), eq(startDate), eq(false), eq("de"),
        eq(allowedSpaces))).andReturn(mockEventSearchResults).once();
    //!! IMPORTANT getSize MUST be called imadiatelly
    expect(mockEventSearchResults.getSize()).andReturn(10).once();
    replayDefault();
    assertSame(mockEventSearchResults, eventsMgr.searchEvents(calMock, query));
    verifyDefault();
  }
  
  @Test
  public void testUpdateEventObject_noChange() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    BaseObject srcObj = getFilledCalEventObject(db);
    srcDoc.addXObject(srcObj);
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(db, "space", "trg"));
    BaseObject trgObj = getFilledCalEventObject(db);
    trgDoc.addXObject(trgObj);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();
    

    replayDefault();
    boolean ret = eventsMgr.updateEventObject(srcDoc, trgDoc, true);
    verifyDefault();
    
    assertFalse(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testUpdateEventObject_changed() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    BaseObject srcObj = getFilledCalEventObject(db);
    srcDoc.addXObject(srcObj);
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(db, "space", "trg"));
    BaseObject trgObj = getFilledCalEventObject(db);
    trgDoc.addXObject(trgObj);
    trgObj.setStringValue(CalendarClasses.PROPERTY_TITLE, null);
    boolean save = true;
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();
    xwiki.saveDocument(same(trgDoc), eq("CalendarEvent updated"), eq(true), same(context));
    expectLastCall().once();

    replayDefault();
    boolean ret = eventsMgr.updateEventObject(srcDoc, trgDoc, save);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testUpdateEventObject_changed_noSave() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    BaseObject srcObj = getFilledCalEventObject(db);
    srcDoc.addXObject(srcObj);
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(db, "space", "trg"));
    BaseObject trgObj = getFilledCalEventObject(db);
    trgDoc.addXObject(trgObj);
    trgObj.setStringValue(CalendarClasses.PROPERTY_TITLE, null);
    boolean save = false;
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();

    replayDefault();
    boolean ret = eventsMgr.updateEventObject(srcDoc, trgDoc, save);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testUpdateEventObject_noTrgObj() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    BaseObject srcObj = getFilledCalEventObject(db);
    srcDoc.addXObject(srcObj);
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(db, "space", "trg"));
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();
    

    replayDefault();
    boolean ret = eventsMgr.updateEventObject(srcDoc, trgDoc, false);
    verifyDefault();
    
    assertTrue(ret);
    BaseObject trgObj = trgDoc.getXObject(getCalEventClassRef(db));
    assertNotNull(trgObj);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testUpdateEventObject_noSrcObj() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(db, "space", "trg"));
    BaseObject trgObj = getFilledCalEventObject(db);
    trgDoc.addXObject(trgObj);    

    replayDefault();
    try {
      eventsMgr.updateEventObject(srcDoc, trgDoc, false);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }
    verifyDefault();
  }
  
  @Test
  public void testUpdateEventObject_notSameDB() throws Exception {
    XWikiDocument srcDoc = new XWikiDocument(new DocumentReference(db, "space", "src"));
    BaseObject srcObj = getFilledCalEventObject(db);
    srcDoc.addXObject(srcObj);
    XWikiDocument trgDoc = new XWikiDocument(new DocumentReference(context.getDatabase(), 
        "space", "trg"));
    BaseObject trgObj = getFilledCalEventObject(context.getDatabase());
    trgDoc.addXObject(trgObj);
    trgObj.setStringValue(CalendarClasses.PROPERTY_TITLE, null);
    boolean save = true;
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(context.getDatabase())), same(context))
        ).andReturn(getCalEventBaseClass(context.getDatabase())).once();    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).once();
    xwiki.saveDocument(same(trgDoc), eq("CalendarEvent updated"), eq(true), same(context));
    expectLastCall().once();

    replayDefault();
    boolean ret = eventsMgr.updateEventObject(srcDoc, trgDoc, save);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testCopyBaseObject_noChange() throws Exception {
    BaseObject srcObj = getFilledCalEventObject(db);
    BaseObject trgObj = getFilledCalEventObject(db);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).once();

    replayDefault();
    boolean ret = eventsMgr.copyBaseObject(srcObj, trgObj);
    verifyDefault();
    
    assertFalse(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testCopyBaseObject_srcChange() throws Exception {
    BaseObject srcObj = getFilledCalEventObject(db);
    srcObj.setStringValue(CalendarClasses.PROPERTY_TITLE, null);
    BaseObject trgObj = getFilledCalEventObject(db);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).times(2);

    replayDefault();
    boolean ret = eventsMgr.copyBaseObject(srcObj, trgObj);
    verifyDefault();

    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testCopyBaseObject_trgChange() throws Exception {
    BaseObject srcObj = getFilledCalEventObject(db);
    BaseObject trgObj = getFilledCalEventObject(db);
    trgObj.setStringValue(CalendarClasses.PROPERTY_TITLE, null);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).times(2);

    replayDefault();
    boolean ret = eventsMgr.copyBaseObject(srcObj, trgObj);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testCopyBaseObject_trgEmpty() throws Exception {
    BaseObject srcObj = getFilledCalEventObject(db);
    BaseObject trgObj = getEmptyCalEventObject(db);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();

    replayDefault();
    boolean ret = eventsMgr.copyBaseObject(srcObj, trgObj);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  @Test
  public void testCopyBaseObject_srcEmpty() throws Exception {
    BaseObject srcObj = getEmptyCalEventObject(db);
    BaseObject trgObj = getFilledCalEventObject(db);
    
    expect(xwiki.getXClass(eq(getCalEventClassRef(db)), same(context))).andReturn(
        getCalEventBaseClass(db)).anyTimes();

    replayDefault();
    boolean ret = eventsMgr.copyBaseObject(srcObj, trgObj);
    verifyDefault();
    
    assertTrue(ret);
    assertObjects(srcObj, trgObj);
  }
  
  private void assertObjects(BaseObject obj1, BaseObject obj2) throws XWikiException {
    Set<String> properties = new HashSet<String>(obj1.getPropertyList());
    properties.addAll(obj2.getPropertyList());
    for (String name : properties) {
      BaseProperty prop1 = (BaseProperty) obj1.get(name);
      Object val1 = prop1 != null ? prop1.getValue() : null;
      BaseProperty prop2 = (BaseProperty) obj2.get(name);
      Object val2 = prop2 != null ? prop2.getValue() : null;
      assertEquals(val1, val2);
    }
  }
  
  private BaseObject getEmptyCalEventObject(String db) {
    BaseObject bObj = new BaseObject();
    bObj.setXClassReference(getCalEventClassRef(db));
    return bObj;
  }
  
  private BaseObject getFilledCalEventObject(String db) {
    BaseObject bObj = getEmptyCalEventObject(db);
    bObj.setStringValue(CalendarClasses.PROPERTY_LANG, "val");
    bObj.setStringValue(CalendarClasses.PROPERTY_TITLE, "val");
    bObj.setStringValue(CalendarClasses.PROPERTY_TITLE_RTE, "val");
    bObj.setStringValue(CalendarClasses.PROPERTY_DESCRIPTION, "val");
    bObj.setStringValue(CalendarClasses.PROPERTY_LOCATION, "val");
    bObj.setStringValue(CalendarClasses.PROPERTY_LOCATION_RTE, "val");
    bObj.setDateValue(CalendarClasses.PROPERTY_EVENT_DATE, new Date(0));
    bObj.setDateValue(CalendarClasses.PROPERTY_EVENT_DATE_END, new Date(0));
    bObj.setIntValue(CalendarClasses.PROPERTY_EVENT_IS_SUBSCRIBABLE, 0);
    return bObj;
  }
  
  private BaseClass getCalEventBaseClass(String db) {
    BaseClass bclass = new BaseClass();
    bclass.setDocumentReference(getCalEventClassRef(db));
    bclass.addTextField(CalendarClasses.PROPERTY_LANG, CalendarClasses.PROPERTY_LANG, 30);
    bclass.addTextField(CalendarClasses.PROPERTY_TITLE, CalendarClasses.PROPERTY_TITLE, 
        30);
    bclass.addTextAreaField(CalendarClasses.PROPERTY_TITLE_RTE, 
        CalendarClasses.PROPERTY_TITLE_RTE, 80, 15);
    bclass.addTextAreaField(CalendarClasses.PROPERTY_DESCRIPTION,
        CalendarClasses.PROPERTY_DESCRIPTION, 80, 15);
    bclass.addTextField(CalendarClasses.PROPERTY_LOCATION, 
        CalendarClasses.PROPERTY_LOCATION, 30);
    bclass.addTextAreaField(CalendarClasses.PROPERTY_LOCATION_RTE,
        CalendarClasses.PROPERTY_LOCATION_RTE, 80, 15);
    bclass.addDateField(CalendarClasses.PROPERTY_EVENT_DATE,
        CalendarClasses.PROPERTY_EVENT_DATE, null, 0);
    bclass.addDateField(CalendarClasses.PROPERTY_EVENT_DATE_END, 
        CalendarClasses.PROPERTY_EVENT_DATE_END, null, 0);
    bclass.addBooleanField(CalendarClasses.PROPERTY_EVENT_IS_SUBSCRIBABLE,
        CalendarClasses.PROPERTY_EVENT_IS_SUBSCRIBABLE, "yesno");
    return bclass;
  }
  
  private DocumentReference getCalEventClassRef(String db) {
    return new DocumentReference(db, "Classes", "CalendarEventClass");
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

}
