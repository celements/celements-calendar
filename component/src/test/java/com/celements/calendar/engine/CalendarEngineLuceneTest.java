package com.celements.calendar.engine;

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
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.web.Utils;

public class CalendarEngineLuceneTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  private CalendarEngineLucene engine;
  private IEventSearch eventSearchMock;
  private ILuceneSearchService searchServiceMock;
  private EventSearchResult eventSearchResultMock;
  private ICalendar calMock;
  private String database;

  @Before
  public void setUp_CalendarEngineLuceneTest() {
    engine = (CalendarEngineLucene) Utils.getComponent(ICalendarEngineRole.class, 
        "lucene");
    eventSearchMock = createMockAndAddToDefault(IEventSearch.class);
    engine.injectEventSearch(eventSearchMock);
    searchServiceMock = createMockAndAddToDefault(ILuceneSearchService.class);
    engine.injectSearchService(searchServiceMock);
    eventSearchResultMock = createMockAndAddToDefault(EventSearchResult.class);
    calMock = createMockAndAddToDefault(ICalendar.class);
    database = "xwikidb";
    DocumentReference docRef = new DocumentReference(database, "someSpace", "someCal");
    expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(calMock.getWikiRef()).andReturn(new WikiReference(database)).anyTimes();
  }

  @Test
  public void testGetName() {
    assertEquals("lucene", engine.getName());
  }

  @Test
  public void testGetEngineLimit() {
    int limit = 5;
    expect(searchServiceMock.getResultLimit()).andReturn(limit).once();
    replayDefault();
    assertEquals(limit, engine.getEngineLimit());
    verifyDefault();
  }

  @Test
  public void testGetEvents() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    int offset = 0;
    int limit = 500;
    List<IEvent> eventList = Collections.emptyList();

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(offset), eq(limit))).andReturn(eventList
        ).once();

    replayDefault();
    List<IEvent> ret = engine.getEvents(calMock, offset, limit);
    verifyDefault();

    assertSame(eventList, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void testCountEventsInternal() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getSize()).andReturn(5).once();

    replayDefault();
    long ret = engine.countEventsInternal(calMock);
    verifyDefault();
 
    assertEquals(5, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void testGetFirstEventDate() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class));

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(0), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();
 
    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void testGetFirstEventDate_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class));

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getSize()).andReturn(5).once();
    expect(eventSearchResultMock.getEventList(eq(4), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();
 
    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate", 
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void testGetLastEventDate() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class));

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getSize()).andReturn(5).once();
    expect(eventSearchResultMock.getEventList(eq(4), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();
 
    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void testGetLastEventDate_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    List<IEvent> eventList = Arrays.asList(createMockAndAddToDefault(IEvent.class));

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(0), eq(1))).andReturn(eventList).once();

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();
 
    assertSame(eventList.get(0), ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"myCalSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate", 
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }
  
  @Test
  public void testSearchEvent_isNotArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, null);
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }
  
  @Test
  public void testSearchEvent_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, null);
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate", 
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
  }
  
  @Test
  public void testSearchEvent_withQuery() throws Exception {
    String db = "theDB";
    List<String> sortFields = Arrays.asList("field");
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");

    expectForCalMock(startDate, isArchive, lang, spaces);
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(calMock, new DefaultEventSearchQuery(db, 
        sortFields));
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "(wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertEquals(sortFields, query.getSortFields());
  }

  private void expectForCalMock(Date startDate, boolean isArchive, String lang, 
      List<String> spaces) {
    expect(calMock.getStartDate()).andReturn(startDate).atLeastOnce();
    expect(calMock.isArchive()).andReturn(isArchive).atLeastOnce();
    expect(calMock.getLanguage()).andReturn(lang).atLeastOnce();
    expect(calMock.getAllowedSpaces()).andReturn(spaces).atLeastOnce();
  }

}
