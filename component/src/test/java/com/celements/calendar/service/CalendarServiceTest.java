package com.celements.calendar.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryExecutor;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.DefaultQuery;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CalendarServiceTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

  private CalendarService calService;
  private XWikiContext context;
  private XWiki xwiki;

  private QueryManager queryManagerMock;
  private QueryExecutor queryExecutorMock;

  @Before
  public void setUp_CalendarServiceTest() throws Exception {
    context = getContext();
    calService = (CalendarService) Utils.getComponent(ICalendarService.class);
    xwiki = createDefaultMock(XWiki.class);
    context.setWiki(xwiki);
    queryManagerMock = createDefaultMock(QueryManager.class);
    queryExecutorMock = createDefaultMock(QueryExecutor.class);
    calService.injectQueryManager(queryManagerMock);
    setCalCache(null);
  }

  @Test
  public void testGetAllCalendars_wiki_exclude() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    DocumentReference toExclude = new DocumentReference("db", "space", "toExclude");
    List<DocumentReference> excludes = Arrays.asList(toExclude);
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    calCache.put(wikiRef, Arrays.asList(new DocumentReference("db", "space", "calDoc1"), toExclude,
        new DocumentReference("db", "space", "calDoc2")));
    setCalCache(calCache);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(wikiRef, excludes);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(calCache.get(wikiRef).get(0), ret.get(0));
    assertEquals(calCache.get(wikiRef).get(2), ret.get(1));
  }

  @Test
  public void testGetAllCalendars_nullWiki() throws Exception {
    WikiReference wikiRef = new WikiReference("xwikidb");
    List<DocumentReference> excludes = Collections.emptyList();
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    calCache.put(wikiRef, Arrays.asList(new DocumentReference("xwikidb", "space", "calDoc1"),
        new DocumentReference("xwikidb", "space", "calDoc2")));
    setCalCache(calCache);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(null, excludes);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(calCache.get(wikiRef).get(0), ret.get(0));
    assertEquals(calCache.get(wikiRef).get(1), ret.get(1));
  }

  @Test
  public void testGetAllCalendars_noCals() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> excludes = Collections.emptyList();
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    calCache.put(wikiRef, new ArrayList<DocumentReference>());
    setCalCache(calCache);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(wikiRef, excludes);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void testGetAllCalendarsInternal_noCache() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Query query = new DefaultQuery("theStatement", Query.XWQL, queryExecutorMock);
    List<Object> fullNames = Arrays.asList("space.calDoc1", "space.calDoc2");

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andReturn(query).once();
    expect(queryExecutorMock.execute(eq(query))).andReturn(fullNames).once();

    assertNull(getCalCache());
    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(new DocumentReference("db", "space", "calDoc1"), ret.get(0));
    assertEquals(new DocumentReference("db", "space", "calDoc2"), ret.get(1));
    assertEquals(1, getCalCache().size());
    assertEquals(ret, getCalCache().get(wikiRef));
  }

  @Test
  public void testGetAllCalendarsInternal_fromQuery() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    calCache.put(new WikiReference("db2"), Arrays.asList(new DocumentReference("db2", "space",
        "calDoc1"), new DocumentReference("db2", "space", "calDoc2")));
    setCalCache(calCache);
    Query query = new DefaultQuery("theStatement", Query.XWQL, queryExecutorMock);
    List<Object> fullNames = Collections.emptyList();

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andReturn(query).once();
    expect(queryExecutorMock.execute(eq(query))).andReturn(fullNames).once();

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
    assertEquals(0, calCache.get(wikiRef).size());
  }

  @Test
  public void testGetAllCalendarsInternal_fromCache() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    calCache.put(wikiRef, Arrays.asList(new DocumentReference("db", "space", "calDoc1"),
        new DocumentReference("db", "space", "calDoc2")));
    setCalCache(calCache);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(calCache.get(wikiRef), ret);
  }

  @Test
  public void testExecuteAllCalendarsQuery() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    Query query = new DefaultQuery("theStatement", Query.XWQL, queryExecutorMock);
    List<Object> fullNames = Arrays.asList("space.calDoc1", "space.calDoc2");

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andReturn(query).once();
    expect(queryExecutorMock.execute(eq(query))).andReturn(fullNames).once();

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(new DocumentReference("db", "space", "calDoc1"), ret.get(0));
    assertEquals(new DocumentReference("db", "space", "calDoc2"), ret.get(1));
    assertEquals("db", query.getWiki());
    assertEquals(0, query.getNamedParameters().size());
  }

  @Test
  public void testExecuteAllCalendarsQuery_empty() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    Query query = new DefaultQuery("theStatement", Query.XWQL, queryExecutorMock);
    List<Object> fullNames = Collections.emptyList();

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andReturn(query).once();
    expect(queryExecutorMock.execute(eq(query))).andReturn(fullNames).once();

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
    assertEquals("db", query.getWiki());
    assertEquals(0, query.getNamedParameters().size());
  }

  @Test
  public void testExecuteAllCalendarsQuery_exception() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    Throwable cause = new QueryException("", null, null);

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andThrow(cause).once();

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void testGetAllCalendarsXWQL() {
    assertEquals("from doc.object(Classes.CalendarConfigClass) as cal "
        + "where doc.translation = 0", calService.getAllCalendarsXWQL());
  }

  @Test
  public void testGetEventSpaceRefForCalendar_noObject() throws XWikiException {
    WikiReference wikiRef = new WikiReference("theDB");
    DocumentReference calDocRef = new DocumentReference(wikiRef.getName(), "mySpace", "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    SpaceReference spaceRef = calService.getEventSpaceRefForCalendar(calDocRef);
    verifyDefault();

    assertEquals(new SpaceReference(calDocRef.getName(), wikiRef), spaceRef);
  }

  @Test
  public void testGetEventSpaceRefForCalendar_emptyObject() throws XWikiException {
    WikiReference wikiRef = new WikiReference("theDB");
    DocumentReference calDocRef = new DocumentReference(wikiRef.getName(), "mySpace", "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(new DocumentReference(wikiRef.getName(), "Classes",
        "CalendarConfigClass"));
    calDoc.setXObject(0, calConfObj);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    SpaceReference spaceRef = calService.getEventSpaceRefForCalendar(calDocRef);
    verifyDefault();

    assertEquals(new SpaceReference(calDocRef.getName(), wikiRef), spaceRef);
  }

  @Test
  public void testGetEventSpaceRefForCalendar() throws XWikiException {
    WikiReference wikiRef = new WikiReference("theDB");
    DocumentReference calDocRef = new DocumentReference(wikiRef.getName(), "mySpace", "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(new DocumentReference(wikiRef.getName(), "Classes",
        "CalendarConfigClass"));
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    SpaceReference spaceRef = calService.getEventSpaceRefForCalendar(calDocRef);
    verifyDefault();

    assertEquals(new SpaceReference("myCalSpace", wikiRef), spaceRef);
  }

  @Test
  public void testGetEventSpaceForCalendar_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

    assertEquals(calDocRef.getName(), space);
  }

  @Test
  public void testGetEventSpaceForCalendar_emptyObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

    assertEquals(calDocRef.getName(), space);
  }

  @Test
  public void testGetEventSpaceForCalendar() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

    assertEquals("myCalSpace", space);
  }

  @Test
  public void testGetEventSpaceForCalendar_differentDB() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference("db", "mySpace", "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference("db", "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

    assertEquals("myCalSpace", space);
  }

  @Test
  public void testGetAllowedSpaces_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

    assertEquals(0, spaces.size());
  }

  @Test
  public void testGetAllowedSpaces_emptyObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

    assertEquals(0, spaces.size());
  }

  @Test
  public void testGetAllowedSpaces() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

    assertEquals(1, spaces.size());
    assertEquals("myCalSpace", spaces.get(0));
  }

  @Test
  public void testGetAllowedSpaces_subscribers() throws XWikiException {
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");

    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");
    List<String> subscribers = Arrays.asList("mySpace.myCalDoc2", "mySpace.myCalDoc3");
    calConfObj.setStringListValue("subscribe_to", subscribers);

    DocumentReference calDocRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc2");
    XWikiDocument calDoc2 = new XWikiDocument(calDocRef2);
    BaseObject calConfObj2 = new BaseObject();
    calConfObj2.setXClassReference(configClassRef);
    calDoc2.setXObject(0, calConfObj2);
    calConfObj2.setStringValue("calendarspace", "myCalSpace2");

    DocumentReference calDocRef3 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc3");
    XWikiDocument calDoc3 = new XWikiDocument(calDocRef3);
    BaseObject calConfObj3 = new BaseObject();
    calConfObj3.setXClassReference(configClassRef);
    calDoc3.setXObject(0, calConfObj3);
    calConfObj3.setStringValue("calendarspace", "myCalSpace3");

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(context))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(context))).andReturn(calDoc3).once();

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

    assertEquals(3, spaces.size());
    assertEquals("myCalSpace", spaces.get(0));
    assertEquals("myCalSpace2", spaces.get(1));
    assertEquals("myCalSpace3", spaces.get(2));
  }

  @Test
  public void testGetAllowedSpacesHQL_none() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content", "Agenda");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

    assertEquals("(obj.name like '.%')", spacesHQL);
  }

  @Test
  public void testGetAllowedSpacesHQL() throws Exception {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

    assertEquals("(obj.name like 'myCalSpace.%')", spacesHQL);
  }

  @Test
  public void testGetAllowedSpacesHQL_subscribers() throws XWikiException {
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(), "Classes",
        "CalendarConfigClass");

    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");
    List<String> subscribers = Arrays.asList("mySpace.myCalDoc2", "mySpace.myCalDoc3");
    calConfObj.setStringListValue("subscribe_to", subscribers);

    DocumentReference calDocRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc2");
    XWikiDocument calDoc2 = new XWikiDocument(calDocRef2);
    BaseObject calConfObj2 = new BaseObject();
    calConfObj2.setXClassReference(configClassRef);
    calDoc2.setXObject(0, calConfObj2);
    calConfObj2.setStringValue("calendarspace", "myCalSpace2");

    DocumentReference calDocRef3 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc3");
    XWikiDocument calDoc3 = new XWikiDocument(calDocRef3);
    BaseObject calConfObj3 = new BaseObject();
    calConfObj3.setXClassReference(configClassRef);
    calDoc3.setXObject(0, calConfObj3);
    calConfObj3.setStringValue("calendarspace", "myCalSpace3");

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(context))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(context))).andReturn(calDoc3).once();

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

    assertEquals("(obj.name like 'myCalSpace.%' or obj.name like 'myCalSpace2.%'"
        + " or obj.name like 'myCalSpace3.%')", spacesHQL);
  }

  @Test
  public void testGetCalendarDocRefByCalendarSpace() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "notMyInSpace",
        "doc"), new DocumentReference(database, "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace, "myInSpace");
    verifyDefault();
    assertEquals(calSpaceCache.get(calSpace).get(1), ret);
  }

  @Test
  public void testGetCalendarDocRefByCalendarSpace_empty() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_noInSpace() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", "doc1"),
        new DocumentReference(database, "notMyInSpace", "doc"), new DocumentReference(database,
            "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    verifyDefault();
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_noInSpace_empty() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_inSpace() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", "doc1"),
        new DocumentReference(database, "notMyInSpace", "doc"), new DocumentReference(database,
            "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace,
        "myInSpace");
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(1));
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_inRef_wiki() throws Exception {
    String database = "db";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", "doc1"),
        new DocumentReference(database, "notMyInSpace", "doc"), new DocumentReference(database,
            "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace,
        new WikiReference(database));
    verifyDefault();
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_inRef_space() throws Exception {
    String database = "db";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", "doc1"),
        new DocumentReference(database, "notMyInSpace", "doc"), new DocumentReference(database,
            "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace,
        new SpaceReference("myInSpace", new WikiReference(database)));
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(1));
  }

  @Test
  public void testGetCalendarDocRefsByCalendarSpace_inRef_null() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", "doc1"),
        new DocumentReference(database, "notMyInSpace", "doc"), new DocumentReference(database,
            "myInSpace", "doc2")));
    setCalSpaceCache(database, calSpaceCache);

    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace,
        (EntityReference) null);
    verifyDefault();
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
  }

  @Test
  public void testGetCalSpaceCache_inject() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Map<String, List<DocumentReference>> calSpaceCache = new HashMap<>();
    setCalSpaceCache("db", calSpaceCache);

    replayDefault();
    Map<String, List<DocumentReference>> ret = calService.getCalSpaceCache(wikiRef);
    verifyDefault();
    assertSame(calSpaceCache, ret);
  }

  @Test
  public void testGetCalSpaceCache() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    String calSpace1 = "calSpace1";
    String calSpace2 = "calSpace2";
    SpaceReference calConfigSpaceRef = new SpaceReference("space", wikiRef);
    DocumentReference calDocRef1 = new DocumentReference("calDoc1", calConfigSpaceRef);
    DocumentReference calDocRef2 = new DocumentReference("calDoc2", calConfigSpaceRef);
    DocumentReference calDocRef3 = new DocumentReference("calDoc3", calConfigSpaceRef);
    Map<WikiReference, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(wikiRef, Arrays.asList(calDocRef1, calDocRef2, calDocRef3));
    setCalCache(calSpaceCache);
    XWikiDocument calDoc1 = new XWikiDocument(calDocRef1);
    setCalConfigObj(calDoc1, calSpace1);
    XWikiDocument calDoc2 = new XWikiDocument(calDocRef2);
    setCalConfigObj(calDoc2, calSpace2);
    XWikiDocument calDoc3 = new XWikiDocument(calDocRef3);
    setCalConfigObj(calDoc3, calSpace1);

    expect(xwiki.getDocument(eq(calDocRef1), same(context))).andReturn(calDoc1).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(context))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(context))).andReturn(calDoc3).once();

    replayDefault();
    Map<String, List<DocumentReference>> ret = calService.getCalSpaceCache(wikiRef);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(2, ret.get(calSpace1).size());
    assertEquals(calDocRef1, ret.get(calSpace1).get(0));
    assertEquals(calDocRef3, ret.get(calSpace1).get(1));
    assertEquals(1, ret.get(calSpace2).size());
    assertEquals(calDocRef2, ret.get(calSpace2).get(0));
    assertSame(ret, getCalSpaceCache(wikiRef.getName()));
  }

  @Test
  public void testGetCalSpaceCache_noCal() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Map<WikiReference, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(wikiRef, new ArrayList<DocumentReference>());
    setCalCache(calSpaceCache);

    replayDefault();
    Map<String, List<DocumentReference>> ret = calService.getCalSpaceCache(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
    assertSame(ret, getCalSpaceCache(wikiRef.getName()));
  }

  @Test
  public void testGetCalSpaceCache_exception() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    SpaceReference calConfigSpaceRef = new SpaceReference("space", wikiRef);
    DocumentReference calDocRef1 = new DocumentReference("calDoc1", calConfigSpaceRef);
    Map<WikiReference, List<DocumentReference>> calSpaceCache = new HashMap<>();
    calSpaceCache.put(wikiRef, Arrays.asList(calDocRef1));
    setCalCache(calSpaceCache);

    expect(xwiki.getDocument(eq(calDocRef1), same(context))).andThrow(new XWikiException()).once();

    replayDefault();
    Map<String, List<DocumentReference>> ret = calService.getCalSpaceCache(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
    assertNull(getCalSpaceCache(wikiRef.getName()));
  }

  @Test
  public void testFilterForSpaceRef_null() throws Exception {
    List<DocumentReference> calDocRefs = new ArrayList<>();
    calDocRefs.add(new DocumentReference("db", "space", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "otherSpace", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "space", "calDoc2"));

    List<DocumentReference> ret = calService.filterForSpaceRef(calDocRefs, null);
    assertNotSame(calDocRefs, ret);
    assertEquals(calDocRefs, ret);
  }

  @Test
  public void testFilterForSpaceRef() throws Exception {
    List<DocumentReference> calDocRefs = new ArrayList<>();
    calDocRefs.add(new DocumentReference("db", "space", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "otherSpace", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "space", "calDoc2"));
    SpaceReference inSpaceRef = new SpaceReference("space", new WikiReference("db"));

    List<DocumentReference> ret = calService.filterForSpaceRef(calDocRefs, inSpaceRef);
    assertNotSame(calDocRefs, ret);
    assertEquals(3, calDocRefs.size());
    assertEquals(2, ret.size());
    assertEquals(calDocRefs.get(0), ret.get(0));
    assertEquals(calDocRefs.get(2), ret.get(1));
  }

  @Test
  public void testIsMidnightDate_true() throws Exception {
    Date date = SDF.parse("20140513000000");
    assertTrue(calService.isMidnightDate(date));
  }

  @Test
  public void testIsMidnightDate_false() throws Exception {
    Date date = SDF.parse("20140513163132");
    assertFalse(calService.isMidnightDate(date));
    assertTrue(calService.isMidnightDate(calService.getMidnightDate(date)));
  }

  @Test
  public void testGetMidnightDate() throws Exception {
    Date date = SDF.parse("20140513163132");
    Date midnightDate = calService.getMidnightDate(date);
    assertEquals(SDF.parse("20140513000000"), midnightDate);
  }

  @Test
  public void testGetMidnightDate_noChange() throws Exception {
    Date date = SDF.parse("20140513000000");
    Date midnightDate = calService.getMidnightDate(date);
    assertEquals(date, midnightDate);
  }

  @Test
  public void testGetEndOfDay() throws Exception {
    Date date = SDF.parse("20140513163132");
    Date endOfDayDate = calService.getEndOfDayDate(date);
    assertEquals(SDF.parse("20140513235959"), endOfDayDate);
  }

  @Test
  public void testGetEndOfDay_noChange() throws Exception {
    Date date = SDF.parse("20140513235959");
    Date endOfDayDate = calService.getEndOfDayDate(date);
    assertEquals(date, endOfDayDate);
  }

  private void setCalConfigObj(XWikiDocument calDoc, String calSpaceName) {
    BaseObject calConfObj = new BaseObject();
    DocumentReference configClassRef = new DocumentReference(
        calDoc.getDocumentReference().getWikiReference().getName(), "Classes",
        "CalendarConfigClass");
    calConfObj.setXClassReference(configClassRef);
    calConfObj.setStringValue("calendarspace", calSpaceName);
    calDoc.addXObject(calConfObj);
  }

  private void setCalCache(Map<WikiReference, List<DocumentReference>> calCache) {
    Utils.getComponent(Execution.class).getContext().setProperty(
        CalendarService.EXECUTIONCONTEXT_KEY_CAL_CACHE, calCache);
  }

  @SuppressWarnings("unchecked")
  private Map<WikiReference, List<DocumentReference>> getCalCache() {
    return (Map<WikiReference, List<DocumentReference>>) Utils.getComponent(
        Execution.class).getContext().getProperty(CalendarService.EXECUTIONCONTEXT_KEY_CAL_CACHE);
  }

  private void setCalSpaceCache(String database,
      Map<String, List<DocumentReference>> calSpaceCache) {
    Utils.getComponent(Execution.class).getContext().setProperty(
        CalendarService.EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + database, calSpaceCache);
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<DocumentReference>> getCalSpaceCache(String database) {
    return (Map<String, List<DocumentReference>>) Utils.getComponent(
        Execution.class).getContext().getProperty(
            CalendarService.EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + database);
  }

}
