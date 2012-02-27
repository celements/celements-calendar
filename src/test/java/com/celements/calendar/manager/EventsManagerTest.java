package com.celements.calendar.manager;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class EventsManagerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private EventsManager eventsMgr;
  private XWiki xwiki;
  private XWikiStoreInterface mockStore;
  private ICalendarService calServiceMock;
  private QueryManager queryManagerMock;
  private EntityReferenceResolver<String> stringRefResolverMock;
  private EntityReferenceSerializer<String> refDefaultSerializerMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp_GetEventsCommandTest() throws Exception {
    context = getContext();
    eventsMgr = new EventsManager();
    eventsMgr.execution = Utils.getComponent(Execution.class);
    calServiceMock = createMock(ICalendarService.class);
    eventsMgr.calService = calServiceMock;
    queryManagerMock = createMock(QueryManager.class);
    eventsMgr.queryManager = queryManagerMock;
    stringRefResolverMock = createMock(EntityReferenceResolver.class);
    eventsMgr.stringRefResolver = stringRefResolverMock;
    refDefaultSerializerMock = createMock(EntityReferenceSerializer.class);
    eventsMgr.refDefaultSerializer = refDefaultSerializerMock;
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockStore).anyTimes();
  }

  @Test
  public void testIsHomeCalendar() throws Exception {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
    "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
        "Event1");
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef))).andReturn("inbox"
        ).once();
    replayAll();
    Event theEvent = new Event(eventDocRef);
    assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
        + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, theEvent));
    verifyAll();
  }

  @Test
  public void testCountEvents_emptyList() throws Exception {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    expect(refDefaultSerializerMock.serialize(eq(calDocRef))).andReturn(
        "mySpace.myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    List<Object> resultList = Collections.emptyList();
    Query queryMock = createStrictMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.HQL))).andReturn(
        queryMock).once();
    expect(queryMock.execute()).andReturn(resultList).once();
    expect(calServiceMock.getAllowedSpacesHQL(same(calDoc))).andReturn("").once();
    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc
        ).atLeastOnce();
    replayAll(queryMock);
    long countEvent = eventsMgr.countEvents(calDoc, false, new Date());
    assertNotNull(countEvent);
    assertEquals(0L, countEvent);
    verifyAll(queryMock);
  }

  @Test
  public void testCountEvents_checkCache() throws Exception {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    expect(refDefaultSerializerMock.serialize(eq(calDocRef))).andReturn(
        "mySpace.myCalDoc");
    Date startDate = new Date();
    String cacheKey = "EventsManager.countEvents|" + "mySpace.myCalDoc" + "|false|"
      + startDate.getTime();
    eventsMgr.execution.getContext().setProperty(cacheKey, 12345L);
    replayAll();
    assertEquals(12345L, eventsMgr.countEvents(calDocRef, false, startDate));
    verifyAll();
  }

  @Test
  public void testGetNavigationDetails() throws Exception {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference calConfObjClassRef = new DocumentReference(context.getDatabase(), 
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE, 
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
    DocumentReference cal1Ref = new DocumentReference(context.getDatabase(), "mySpace",
      "cal1");
    XWikiDocument cal1Doc = new XWikiDocument(cal1Ref);
    BaseObject cal1ConfigObj = new BaseObject();
    cal1ConfigObj.setXClassReference(calConfObjClassRef);
    cal1Doc.setXObject(0, cal1ConfigObj);
    cal1ConfigObj.setStringValue(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE,
        "myEventSpace");
    Calendar theCal = new Calendar(cal1Doc, false, context);
    expect(xwiki.getWebPreference(eq("default_language"), eq("myEventSpace"), eq(""),
        same(context))).andReturn("de").anyTimes();
    BaseObject eventObj = createMockEventDoc("Event1");
    expect(calServiceMock.getAllowedSpacesHQL(same(cal1Doc))).andReturn("").atLeastOnce();
    expect(calServiceMock.getEventSpaceForCalendar(eq(cal1Ref))).andReturn("myEventSpace"
      ).atLeastOnce();
    Query mockQuery = createStrictMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.HQL))).andReturn(
        mockQuery).atLeastOnce();
    expect(mockQuery.setOffset(eq(0))).andReturn(mockQuery).once();
    expect(mockQuery.setLimit(eq(10))).andReturn(mockQuery).once();
    List<String> result1 = Arrays.asList("myEventSpace.Event2",
        "myEventSpace.Event3", "myEventSpace.Event4", "myEventSpace.Event5",
        "myEventSpace.Event6", "myEventSpace.Event7", "myEventSpace.Event8",
        "myEventSpace.Event9", "myEventSpace.Event10", "myEventSpace.Event10");
    List<Object> result1List = new ArrayList<Object>(result1);
    addToResolver(result1);
    expect(mockQuery.execute()).andReturn(result1List).once();
    expect(mockQuery.setOffset(eq(10))).andReturn(mockQuery).once();
    expect(mockQuery.setLimit(eq(20))).andReturn(mockQuery).once();
    List<String> result2 = Arrays.asList("myEventSpace.Event11", "myEventSpace.Event12",
        "myEventSpace.Event13", "myEventSpace.Event15", "myEventSpace.Event16",
        "myEventSpace.Event17", "myEventSpace.Event18", "myEventSpace.Event19",
        "myEventSpace.Event14", "myEventSpace.Event20", "myEventSpace.Event21",
        "myEventSpace.Event22", "myEventSpace.Event23", "myEventSpace.Event25",
        "myEventSpace.Event26", "myEventSpace.Event1", "myEventSpace.Event28",
        "myEventSpace.Event29", "myEventSpace.Event24", "myEventSpace.Event30");
    List<Object> result2List = new ArrayList<Object>(result2);
    addToResolver(result2);
    expect(mockQuery.execute()).andReturn(result2List).once();
    expect(xwiki.getDocument(eq(cal1Ref), same(context))).andReturn(cal1Doc
      ).atLeastOnce();
    replayAll(mockQuery);
    Event theEvent = new Event(Arrays.asList(eventObj), "myEventSpace");
    assertEquals(new NavigationDetails(theEvent.getEventDate(), 25),
        eventsMgr.getNavigationDetails(theEvent, theCal));
    verifyAll(mockQuery);
  }


  private void addToResolver(List<String> fullNameList) throws XWikiException{
    for(String fullName : fullNameList) {
      EntityReference docRef = new DocumentReference(" ", fullName.split("\\.")[0],
          fullName.split("\\.")[1]);
      expect(stringRefResolverMock.resolve(eq(fullName), eq(EntityType.DOCUMENT))
          ).andReturn(docRef).once();
      createMockEventDoc(fullName.split("\\.")[1]);
    }
  }

  private BaseObject createMockEventDoc(String eventDocName) throws XWikiException {
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", eventDocName);
    XWikiDocument eventDoc = new XWikiDocument(eventDocRef);
    BaseObject eventObj = new BaseObject();
    eventObj.setStringValue("", "de");
    eventObj.setDateValue(CelementsCalendarPlugin.PROPERTY_EVENT_DATE, new Date());
    eventDoc.setXObject(0, eventObj );
    return eventObj;
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, mockStore, calServiceMock, queryManagerMock,
        stringRefResolverMock, refDefaultSerializerMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockStore, calServiceMock, queryManagerMock,
        stringRefResolverMock, refDefaultSerializerMock);
    verify(mocks);
  }

}
