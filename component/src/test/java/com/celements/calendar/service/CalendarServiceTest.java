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
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CalendarServiceTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

  private CalendarService calService;

  private QueryManager queryManagerMock;
  private IQueryExecutionServiceRole queryExecServiceMock;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    queryManagerMock = registerComponentMock(QueryManager.class);
    queryExecServiceMock = registerComponentMock(IQueryExecutionServiceRole.class);
    calService = (CalendarService) Utils.getComponent(ICalendarService.class);
    setCalCache(null, null);
  }

  @Test
  public void test_getAllCalendars_wiki_exclude() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> excludes = Arrays.asList(new DocumentReference("db", "space",
        "toExclude"));
    setCalCache(wikiRef, FluentIterable.from(getResultDocRefs(wikiRef)).append(excludes).toList());

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(wikiRef, excludes);
    verifyDefault();
    assertEquals(getResultDocRefs(wikiRef), ret);
  }

  @Test
  public void test_getAllCalendars_nullWiki() throws Exception {
    WikiReference wikiRef = new WikiReference("xwikidb");
    List<DocumentReference> excludes = Collections.emptyList();
    setCalCache(wikiRef, getResultDocRefs(wikiRef));

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(null, excludes);
    verifyDefault();
    assertEquals(getResultDocRefs(wikiRef), ret);
  }

  @Test
  public void test_getAllCalendars_noCals() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> excludes = Collections.emptyList();
    setCalCache(wikiRef, new ArrayList<DocumentReference>());

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendars(wikiRef, excludes);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_getAllCalendarsInternal_noCache() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> result = getResultDocRefs(wikiRef);
    expectAllCalsQuery(wikiRef, result);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(ret, result);
    assertEquals(1, getCalCache().size());
    assertEquals(ret, getCalCache().get(wikiRef));
  }

  @Test
  public void test_getAllCalendarsInternal_fromQuery() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    WikiReference wikiRef2 = new WikiReference("db2");
    setCalCache(wikiRef2, getResultDocRefs(wikiRef2));
    List<DocumentReference> result = Collections.emptyList();
    expectAllCalsQuery(wikiRef, result);

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
    assertEquals(0, getCalCache().get(wikiRef).size());
  }

  @Test
  public void test_getAllCalendarsInternal_fromCache() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    setCalCache(wikiRef, getResultDocRefs(wikiRef));

    replayDefault();
    List<DocumentReference> ret = calService.getAllCalendarsInternal(wikiRef);
    verifyDefault();
    assertEquals(getCalCache().get(wikiRef), ret);
  }

  @Test
  public void test_executeAllCalendarsQuery() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> result = getResultDocRefs(wikiRef);

    expectAllCalsQuery(wikiRef, result);

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(result, ret);
  }

  @Test
  public void test_executeAllCalendarsQuery_empty() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    List<DocumentReference> result = Collections.emptyList();

    expectAllCalsQuery(wikiRef, result);

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_executeAllCalendarsQuery_exception() throws QueryException {
    WikiReference wikiRef = new WikiReference("db");
    Throwable cause = new QueryException("", null, null);

    expect(queryManagerMock.createQuery(eq(calService.getAllCalendarsXWQL()), eq(
        Query.XWQL))).andThrow(cause).once();

    replayDefault();
    List<DocumentReference> ret = calService.executeAllCalendarsQuery(wikiRef);
    verifyDefault();
    assertEquals(0, ret.size());
  }

  private List<DocumentReference> getResultDocRefs(WikiReference wikiRef) {
    SpaceReference spaceRef = new SpaceReference("space", wikiRef);
    return Arrays.asList(new DocumentReference("calDoc1", spaceRef), new DocumentReference(
        "calDoc2", spaceRef));
  }

  private Query expectAllCalsQuery(WikiReference wikiRef, List<DocumentReference> result)
      throws QueryException {
    Query queryMock = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(calService.getAllCalendarsXWQL(), Query.XWQL)).andReturn(
        queryMock);
    expect(queryMock.setWiki(wikiRef.getName())).andReturn(queryMock);
    expect(queryExecServiceMock.executeAndGetDocRefs(same(queryMock))).andReturn(result);
    return queryMock;
  }

  @Test
  public void test_getAllCalendarsXWQL() {
    assertEquals("from doc.object(Classes.CalendarConfigClass) as cal "
        + "where doc.translation = 0", calService.getAllCalendarsXWQL());
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

    expect(xwiki.getDocument(eq(calDocRef1), same(getContext()))).andReturn(calDoc1).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(getContext()))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(getContext()))).andReturn(calDoc3).once();

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
    setCalCache(wikiRef, Collections.<DocumentReference>emptyList());

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
    setCalCache(wikiRef, Arrays.asList(calDocRef1));

    expect(xwiki.getDocument(eq(calDocRef1), same(getContext()))).andThrow(
        new XWikiException()).once();

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
    assertEquals(SDF.parse("20140513235959").getTime(), endOfDayDate.getTime());
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

  private void setCalCache(WikiReference wikiRef, List<DocumentReference> calDocRefs) {
    Map<WikiReference, List<DocumentReference>> calCache = new HashMap<>();
    if (wikiRef != null) {
      calCache.put(wikiRef, calDocRefs);
    }
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
