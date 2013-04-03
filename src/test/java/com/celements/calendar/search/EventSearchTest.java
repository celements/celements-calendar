package com.celements.calendar.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.SearchResult;
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
        "Classes.CalendarEventClass");
    
    expect(queryServiceMock.createRestriction(eq("object"), 
        eq("Classes.CalendarEventClass"))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();
    
    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResult(queryMock);
    verifyAll();
    
    assertNotNull(searchResult);    
    assertEquals("asdf", searchResult.getLuceneQuery());
    String[] sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(1, sortFields.length);
    assertEquals("Classes.CalendarEventClass.eventDate", sortFields[0]);
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
        eq("Classes.CalendarEventClass"))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryServiceMock.createRangeRestriction(
        eq("Classes.CalendarEventClass.eventDate"), eq("197001210100"), 
        eq("999912312359"), eq(true))).andReturn(rangeRestriction).once();
    expect(queryMock.addRestriction(same(rangeRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();
    
    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResultFromDate(queryMock, date);
    verifyAll();
    
    assertNotNull(searchResult);    
    assertEquals("asdf", searchResult.getLuceneQuery());
    String[] sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(1, sortFields.length);
    assertEquals("Classes.CalendarEventClass.eventDate", sortFields[0]);
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
        eq("Classes.CalendarEventClass"))).andReturn(objectRestriction).once();
    expect(queryMock.addRestriction(same(objectRestriction))).andReturn(queryMock).once();
    expect(queryServiceMock.createRangeRestriction(
        eq("Classes.CalendarEventClass.eventDate"), eq("000101010000"), 
        eq("197001210100"), eq(false))).andReturn(rangeRestriction).once();
    expect(queryMock.addRestriction(same(rangeRestriction))).andReturn(queryMock).once();
    expect(queryMock.getQueryString()).andReturn(queryString).once();
    
    replayAll();
    EventSearchResult searchResult = eventSearch.getSearchResultUptoDate(queryMock, date);
    verifyAll();
    
    assertNotNull(searchResult);    
    assertEquals("asdf", searchResult.getLuceneQuery());
    String[] sortFields = searchResult.getSortFields();
    assertNotNull(sortFields);
    assertEquals(1, sortFields.length);
    assertEquals("-Classes.CalendarEventClass.eventDate", sortFields[0]);
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
