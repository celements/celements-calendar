package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class EventSearchTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private LuceneQueryApi queryMock;
  private IQueryService queryServiceMock;
  private EventSearch eventSearch;

  @Before
  public void setUp_EventSearchTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    queryMock = createMock(LuceneQueryApi.class);
    queryServiceMock = createMock(IQueryService.class);
    context.setWiki(xwiki);
    eventSearch = (EventSearch) Utils.getComponent(IEventSearch.class);
    eventSearch.injectQueryService(queryServiceMock);
  }

  @Test
  public void testGetSearchResult() throws Exception {
    String queryString = "asdf";
    LuceneQueryRestrictionApi objectRestriction = new LuceneQueryRestrictionApi("object",
        "\"Classes.CalendarEventClass\"");

    expect(queryServiceMock.createRestriction(eq("object"),
        eq("\"Classes.CalendarEventClass\""))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();

    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResult(queryMock);
    verifyAll();

    assertNotNull(searchResult);
    assertEquals("asdf", searchResult.getLuceneQuery());
    assertFalse(searchResult.getSkipChecks());
    List<String> sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(3, sortFields.size());
    assertEquals("Classes.CalendarEventClass.eventDate", sortFields.get(0));
    assertEquals("Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
    assertEquals("Classes.CalendarEventClass.l_title", sortFields.get(2));
  }

  @Test
  public void testGetSearchResult_withSortFields() throws Exception {
    String queryString = "asdf";
    LuceneQueryRestrictionApi objectRestriction = new LuceneQueryRestrictionApi("object",
        "\"Classes.CalendarEventClass\"");
    List<String> sortFields = Arrays.asList("field1", "field2");

    expect(queryServiceMock.createRestriction(eq("object"),
        eq("\"Classes.CalendarEventClass\""))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();

    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResult(queryMock, sortFields);
    verifyAll();

    assertNotNull(searchResult);
    assertEquals("asdf", searchResult.getLuceneQuery());
    assertFalse(searchResult.getSkipChecks());
    assertEquals(sortFields, searchResult.getSortFields());

  }

  @Test
  public void testGetSearchResultWithoutChecks() throws Exception {
    String queryString = "asdf";
    LuceneQueryRestrictionApi objectRestriction = new LuceneQueryRestrictionApi("object",
        "\"Classes.CalendarEventClass\"");

    expect(queryServiceMock.createRestriction(eq("object"),
        eq("\"Classes.CalendarEventClass\""))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();

    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResultWithoutChecks(queryMock);
    verifyAll();

    assertNotNull(searchResult);
    assertEquals("asdf", searchResult.getLuceneQuery());
    assertTrue(searchResult.getSkipChecks());
    List<String> sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(3, sortFields.size());
    assertEquals("Classes.CalendarEventClass.eventDate", sortFields.get(0));
    assertEquals("Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
    assertEquals("Classes.CalendarEventClass.l_title", sortFields.get(2));
  }

  @Test
  public void testGetSearchResultFromDate() throws Exception {
    Date date = new Date(20*24*60*60*1000);
    String queryString = "asdf";
    LuceneQueryRestrictionApi objectRestriction = new LuceneQueryRestrictionApi("object",
        "asdf");
    LuceneQueryRestrictionApi rangeRestriction = new LuceneQueryRestrictionApi("range",
        "asdf");

    expect(queryServiceMock.createRestriction(eq("object"),
        eq("\"Classes.CalendarEventClass\""))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryServiceMock.createFromDateRestriction(
        eq("Classes.CalendarEventClass.eventDate"), eq(date), eq(true))).andReturn(
            rangeRestriction).once();
    expect(queryMock.addRestriction(same(rangeRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();

    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResultFromDate(queryMock, date);
    verifyAll();

    assertNotNull(searchResult);
    assertEquals("asdf", searchResult.getLuceneQuery());
    List<String> sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(3, sortFields.size());
    assertEquals("Classes.CalendarEventClass.eventDate", sortFields.get(0));
    assertEquals("Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
    assertEquals("Classes.CalendarEventClass.l_title", sortFields.get(2));
  }

  @Test
  public void testGetSearchResultUptoDate() throws Exception {
    Date date = new Date(20*24*60*60*1000);
    String queryString = "asdf";
    LuceneQueryRestrictionApi objectRestriction = new LuceneQueryRestrictionApi("object",
        "asdf");
    LuceneQueryRestrictionApi rangeRestriction = new LuceneQueryRestrictionApi("range",
        "asdf");

    expect(queryServiceMock.createRestriction(eq("object"),
        eq("\"Classes.CalendarEventClass\""))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryServiceMock.createToDateRestriction(
        eq("Classes.CalendarEventClass.eventDate"), eq(date), eq(false))).andReturn(
            rangeRestriction).once();
    expect(queryMock.addRestriction(same(rangeRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();

    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResultUptoDate(queryMock, date);
    verifyAll();

    assertNotNull(searchResult);
    assertEquals("asdf", searchResult.getLuceneQuery());
    List<String> sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(3, sortFields.size());
    assertEquals("-Classes.CalendarEventClass.eventDate", sortFields.get(0));
    assertEquals("-Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
    assertEquals("-Classes.CalendarEventClass.l_title", sortFields.get(2));
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, queryMock, queryServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, queryMock, queryServiceMock);
    verify(mocks);
  }

}
