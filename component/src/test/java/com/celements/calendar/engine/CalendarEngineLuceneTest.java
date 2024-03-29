package com.celements.calendar.engine;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.CalendarEventSearchQuery;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.ICalendarSearchQueryBuilder;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.IEventSearchRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class CalendarEngineLuceneTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  private CalendarEngineLucene engine;
  private IEventSearchRole eventSearchMock;
  private ILuceneSearchService searchServiceMock;
  private EventSearchResult eventSearchResultMock;
  private ICalendar calMock;
  private String database;

  @Before
  public void setUp_CalendarEngineLuceneTest() {
    engine = (CalendarEngineLucene) Utils.getComponent(ICalendarEngineRole.class, "lucene");
    eventSearchMock = createDefaultMock(IEventSearchRole.class);
    engine.injectEventSearchService(eventSearchMock);
    searchServiceMock = createDefaultMock(ILuceneSearchService.class);
    engine.injectSearchService(searchServiceMock);
    eventSearchResultMock = createDefaultMock(EventSearchResult.class);
    calMock = createDefaultMock(ICalendar.class);
    database = "xwikidb";
    DocumentReference docRef = new DocumentReference(database, "someSpace", "someCal");
    expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(calMock.getWikiRef()).andReturn(new WikiReference(database)).anyTimes();
    LucenePlugin lucenePlugin = createDefaultMock(LucenePlugin.class);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(lucenePlugin).anyTimes();
    expect(lucenePlugin.getAnalyzer()).andReturn(null).anyTimes();
  }

  @Test
  public void test_getName() {
    assertEquals("lucene", engine.getName());
  }

  @Test
  public void test_getEngineLimit() {
    int limit = 5;
    boolean skipChecks = true;
    expect(eventSearchMock.skipChecks()).andReturn(skipChecks).once();
    expect(searchServiceMock.getResultLimit(eq(skipChecks))).andReturn(limit).once();
    replayDefault();
    assertEquals(limit, engine.getEngineLimit());
    verifyDefault();
  }

  @Test
  public void test_getEvents() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace");
    int offset = 0;
    int limit = 500;
    List<IEvent> eventList = Collections.emptyList();

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(offset), eq(limit))).andReturn(eventList).once();

    replayDefault();
    List<IEvent> ret = engine.getEvents(calMock, offset, limit);
    verifyDefault();

    assertSame(eventList, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate",
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_countEventsInternal() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace");

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getSize()).andReturn(5).once();

    replayDefault();
    long ret = engine.countEventsInternal(calMock);
    verifyDefault();

    assertEquals(5, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate",
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_getFirstEventDate() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createDefaultMock(IEvent.class));

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(0), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();

    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate",
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_getFirstEventDate_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createDefaultMock(IEvent.class));

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).times(2);
    expect(eventSearchResultMock.getSize()).andReturn(5).once();
    expect(eventSearchResultMock.getEventList(eq(4), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();

    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:({0 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate",
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_getLastEventDate() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createDefaultMock(IEvent.class));

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).times(2);
    expect(eventSearchResultMock.getSize()).andReturn(5).once();
    expect(eventSearchResultMock.getEventList(eq(4), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();

    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate",
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_getLastEventDate_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createDefaultMock(IEvent.class));

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(0), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();

    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.eventDate:({0 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate",
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_searchEvent_isNotArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();

    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, null);
    verifyDefault();

    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate",
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_searchEvent_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();

    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, null);
    verifyDefault();

    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:({0 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate",
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_searchEvent_withQuery() throws Exception {
    WikiReference wikiRef = new WikiReference("mydb");
    List<String> sortFields = Arrays.asList("field");
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, spaces);
    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();

    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, new DefaultEventSearchQuery(wikiRef,
        sortFields));
    verifyDefault();

    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"mydb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO zzzzzzzzzzzz]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_searchEvent_CalendarSearchQueryBuilder() throws Exception {
    WikiReference wikiRef = new WikiReference("mydb");

    Capture<IEventSearchQuery> queryCapture = newCapture();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();

    final ICalendarSearchQueryBuilder queryBuilder = createDefaultMock(
        ICalendarSearchQueryBuilder.class);
    final ICalendarSearchQueryBuilder queryBuilder2 = createDefaultMock(
        ICalendarSearchQueryBuilder.class);
    expect(queryBuilder.getWikiRef()).andReturn(wikiRef).anyTimes();
    expect(queryBuilder.addCalendarRestrictions(same(calMock))).andReturn(queryBuilder2);

    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, queryBuilder);
    verifyDefault();

    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertFalse("If the queryBuilder parameter implements ICalendarSearchQueryBuilder it must not"
        + " be wrapped with an CalendarEventSearchQuery.",
        query.getClass().equals(CalendarEventSearchQuery.class));
    assertSame("getSearchResults must be called with the object returned from"
        + " addCalendarRestrictions", queryBuilder2, query);
  }

  private void expectForCalMock(Date startDate, boolean isArchive, List<String> spaces) {
    expect(calMock.getStartDate()).andReturn(startDate).atLeastOnce();
    expect(calMock.isArchive()).andReturn(isArchive).atLeastOnce();
    expect(calMock.getAllowedSpaces()).andReturn(spaces).atLeastOnce();
  }

}
