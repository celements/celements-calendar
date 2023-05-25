package com.celements.calendar.engine;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.search.IEventSearchRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.ILuceneSearchService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class AbstractCalendarEngineTest extends AbstractComponentTest {

  private CalendarEngineLucene engine;
  private IEventSearchRole eventSearchMock;
  private ILuceneSearchService searchServiceMock;
  private EventSearchResult eventSearchResultMock;
  private ICalendar calMock;
  private String database;

  @Before
  public void setUp_AbstractCalendarEngineTest() {
    engine = (CalendarEngineLucene) Utils.getComponent(ICalendarEngineRole.class, "lucene");
    eventSearchMock = createDefaultMock(IEventSearchRole.class);
    engine.injectEventSearchService(eventSearchMock);
    searchServiceMock = createDefaultMock(ILuceneSearchService.class);
    engine.injectSearchService(searchServiceMock);
    eventSearchResultMock = createDefaultMock(EventSearchResult.class);
    expect(eventSearchMock.getSearchResult(anyObject(IEventSearchQuery.class))).andReturn(
        eventSearchResultMock).anyTimes();
    calMock = createDefaultMock(ICalendar.class);
    database = "xwikidb";
    DocumentReference docRef = new DocumentReference(database, "someSpace", "someCal");
    expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(calMock.getWikiRef()).andReturn(new WikiReference(database)).anyTimes();
  }

  @Test
  public void testCountEvents() throws Exception {
    Date startDate = new Date();
    boolean isArchive = false;
    int count = 5;

    expectForCalMock(startDate, isArchive);
    expect(eventSearchResultMock.getSize()).andReturn(count).once();

    replayDefault();
    long ret = engine.countEvents(calMock);
    String key = engine.getCalCountCacheKey(calMock);
    verifyDefault();

    assertEquals(count, ret);
    assertEquals((long) count, Utils.getComponent(Execution.class).getContext().getProperty(key));
  }

  @Test
  public void testCountEvents_0() throws Exception {
    Date startDate = new Date();
    boolean isArchive = false;
    int count = 0;

    expectForCalMock(startDate, isArchive);
    expect(eventSearchResultMock.getSize()).andReturn(count).once();

    replayDefault();
    long ret = engine.countEvents(calMock);
    String key = engine.getCalCountCacheKey(calMock);
    verifyDefault();

    assertEquals(count, ret);
    assertNull(Utils.getComponent(Execution.class).getContext().getProperty(key));
  }

  @Test
  public void testCountEvents_inCache() throws XWikiException {
    Date startDate = new Date();
    boolean isArchive = false;
    long count = 5;

    expectForCalMock(startDate, isArchive);

    replayDefault();
    String key = engine.getCalCountCacheKey(calMock);
    Utils.getComponent(Execution.class).getContext().setProperty(key, count);
    long ret = engine.countEvents(calMock);
    verifyDefault();

    assertEquals(count, ret);
  }

  @Test
  public void testGetCalCountCacheKey() {
    Date startDate = new Date();
    boolean isArchive = false;

    expectForCalMock(startDate, isArchive);

    replayDefault();
    String ret = engine.getCalCountCacheKey(calMock);
    int hashCode = calMock.hashCode();
    verifyDefault();

    String expectedKey = "CalendarEngine.countEvents|lucene|" + hashCode;
    assertEquals(expectedKey, ret);
  }

  private void expectForCalMock(Date startDate, boolean isArchive) {
    expect(calMock.getStartDate()).andReturn(startDate).anyTimes();
    expect(calMock.isArchive()).andReturn(isArchive).anyTimes();
    expect(calMock.getLanguage()).andReturn("de").anyTimes();
    expect(calMock.getAllowedSpaces()).andReturn(Arrays.asList("myCalSpace")).anyTimes();
  }

}
