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

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.CalendarEventSearchQuery;
import com.celements.calendar.search.DefaultEventSearchQuery;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class CalendarEngineLuceneTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  private XWikiContext context;
  private CalendarEngineLucene engine;
  private IEventSearch eventSearchMock;
  private CalendarEngineHQL engineHQLMock;
  private LucenePlugin lucenePluginMock;
  private EventSearchResult eventSearchResultMock;

  @Before
  public void setUp_EventsManagerTest() {
    context = getContext();
    engine = (CalendarEngineLucene) Utils.getComponent(ICalendarEngineRole.class, "lucene");
    eventSearchMock = createMockAndAddToDefault(IEventSearch.class);
    engine.injectEventSearch(eventSearchMock);
    engineHQLMock = createMockAndAddToDefault(CalendarEngineHQL.class);
    engine.injectHQLEngine(engineHQLMock);
    lucenePluginMock = createMockAndAddToDefault(LucenePlugin.class);
    engine.injectLucenePlugin(lucenePluginMock);
    eventSearchResultMock = createMockAndAddToDefault(EventSearchResult.class);
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

    expect(lucenePluginMock.getResultLimit(eq(false), same(context))).andReturn(1000
        ).once();
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    expect(eventSearchResultMock.getEventList(eq(offset), eq(limit))).andReturn(eventList
        ).once();

    replayDefault();
    List<IEvent> ret = engine.getEvents(startDate, isArchive, lang, spaces, offset, limit);
    verifyDefault();

    assertSame(eventList, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]) "
        + "AND (space:(+\"myCalSpace\")) AND wiki:xwikidb";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
    assertFalse(query.skipChecks());
  }

  @Test
  public void testGetEvents_resultLimit() throws XWikiException {
    Date startDate = new Date();
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    int offset = 500;
    int limit = 501;
    List<IEvent> eventList = Collections.emptyList();

    expect(lucenePluginMock.getResultLimit(eq(false), same(context))).andReturn(1000
        ).once();
    expect(engineHQLMock.getEvents(eq(startDate), eq(isArchive), eq(lang), eq(spaces), eq(
        offset), eq(limit))).andReturn(eventList).once();
    
    replayDefault();
    List<IEvent> ret = engine.getEvents(startDate, isArchive, lang, spaces, offset, limit);
    verifyDefault();

    assertSame(eventList, ret);
  }

  @Test
  public void testCountEvents() throws XWikiException {
    Date startDate = new Date();
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");

    expect(engineHQLMock.countEvents(eq(startDate), eq(false), eq(lang), eq(spaces))
        ).andReturn(2L).once();

    replayDefault();
    long countEvent = engine.countEvents(startDate, false, lang, spaces);
    verifyDefault();

    assertEquals(2L, countEvent);
  }

  @Test
  public void testGetFirstEventDate() {
    Date startDate = new Date(0);
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    IEvent event = new Event(null);

    expect(engineHQLMock.getFirstEvent(eq(startDate), eq(isArchive), eq(lang), eq(spaces))
        ).andReturn(event).once();

    replayDefault();
    IEvent firstEvent = engine.getFirstEvent(startDate, isArchive, lang, spaces);
    verifyDefault();

    assertTrue(firstEvent == event);
  }

  @Test
  public void testGetLastEventDate() {
    Date startDate = new Date(0);
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    IEvent event = new Event(null);

    expect(engineHQLMock.getLastEvent(eq(startDate), eq(isArchive), eq(lang), eq(spaces))
        ).andReturn(event).once();

    replayDefault();
    IEvent lastEvent = engine.getLastEvent(startDate, isArchive, lang, spaces);
    verifyDefault();

    assertTrue(lastEvent == event);
  }
  
  @Test
  public void testSearchEvent_isNotArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1, myCalSpace2");
    
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(null, startDate, isArchive, lang, spaces);
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]) "
        + "AND (space:(+\"myCalSpace1, myCalSpace2\")) AND wiki:xwikidb";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("Classes.CalendarEventClass.eventDate", 
        "Classes.CalendarEventClass.eventDate_end", "Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
    assertFalse(query.skipChecks());
  }
  
  @Test
  public void testSearchEvent_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1, myCalSpace2");
    
    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(null, startDate, isArchive, lang, spaces);
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}) "
        + "AND (space:(+\"myCalSpace1, myCalSpace2\")) AND wiki:xwikidb";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    List<String> sortFields = Arrays.asList("-Classes.CalendarEventClass.eventDate", 
        "-Classes.CalendarEventClass.eventDate_end", "-Classes.CalendarEventClass.l_title");
    assertEquals(sortFields, query.getSortFields());
    assertFalse(query.skipChecks());
  }
  
  @Test
  public void testSearchEvent_withQuery() throws Exception {
    String db = "theDB";
    List<String> sortFields = Arrays.asList("field");
    boolean skipChecks = true;
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1, myCalSpace2");

    Capture<IEventSearchQuery> queryCapture = new Capture<IEventSearchQuery>();
    expect(eventSearchMock.getSearchResult(capture(queryCapture))).andReturn(
        eventSearchResultMock).once();
    
    replayDefault();
    EventSearchResult ret = engine.searchEvents(new DefaultEventSearchQuery(db, 
        sortFields, skipChecks), startDate, isArchive, lang, spaces);
    verifyDefault();
    
    assertSame(eventSearchResultMock, ret);
    IEventSearchQuery query = queryCapture.getValue();
    assertTrue(query instanceof CalendarEventSearchQuery);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]) "
        + "AND (space:(+\"myCalSpace1, myCalSpace2\")) AND wiki:theDB";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertEquals(sortFields, query.getSortFields());
    assertTrue(query.skipChecks());   
  }

}
