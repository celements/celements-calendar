package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;

public class EventSearchResultTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private LucenePlugin lucenePluginMock;
  private SearchResults searchResultsMock;

  @Before
  public void setUp_EventSearchResultTest() throws Exception {
    context = getContext();
    lucenePluginMock = createMockAndAddToDefault(LucenePlugin.class);
    searchResultsMock = createMockAndAddToDefault(SearchResults.class);
  }
  
  @Test
  public void testLuceneSearch() throws Exception {
    String queryString = "theQuery";
    String[] sortFields = new String[] {"field1", "field2"};
    EventSearchResult searchResult = getEventSearchResult(queryString, sortFields, false);

    Capture<String[]> sortFieldsCapture = new Capture<String[]>();
    expect(lucenePluginMock.getSearchResults(eq(queryString), capture(sortFieldsCapture), 
        isNull(String.class), eq("default,de"), same(context))).andReturn(
        searchResultsMock).once();
    
    replayDefault();
    SearchResults ret = searchResult.luceneSearch();
    verifyDefault();
    assertSame(searchResultsMock, ret);
    assertEquals(Arrays.asList(sortFields), Arrays.asList(sortFieldsCapture.getValue()));
  }
  
  @Test
  public void testLuceneSearch_skipChecks() throws Exception {
    String queryString = "theQuery";
    String[] sortFields = new String[] {"field1", "field2"};
    EventSearchResult searchResult = getEventSearchResult(queryString, sortFields, true);

    Capture<String[]> sortFieldsCapture = new Capture<String[]>();
    expect(lucenePluginMock.getSearchResultsWithoutChecks(eq(queryString), 
        capture(sortFieldsCapture), isNull(String.class), eq("default,de"), same(context))
        ).andReturn(searchResultsMock).once();
    
    replayDefault();
    assertSame(searchResultsMock, searchResult.luceneSearch());
    verifyDefault();
    assertEquals(Arrays.asList(sortFields), Arrays.asList(sortFieldsCapture.getValue()));
  }
  
  @Test
  public void testGetEventList() throws Exception {
    String queryString = "theQuery";
    String[] sortFields = new String[] {"field1", "field2"};
    EventSearchResult searchResult = getEventSearchResult(queryString, sortFields, false);
    int offset = 5;
    int limit = 10;
    
    SearchResult mockSearchResult1 = createMockAndAddToDefault(SearchResult.class);
    SearchResult mockSearchResult2 = createMockAndAddToDefault(SearchResult.class);
    List<SearchResult> searchResultList = new ArrayList<SearchResult>();
    searchResultList.add(mockSearchResult1);
    searchResultList.add(mockSearchResult2);

    Capture<String[]> sortFieldsCapture = new Capture<String[]>();
    expect(lucenePluginMock.getSearchResults(eq(queryString), capture(sortFieldsCapture), 
        isNull(String.class), eq("default,de"), same(context))).andReturn(
        searchResultsMock).once();
    expect(searchResultsMock.getResults(eq(offset + 1), eq(limit))).andReturn(
        searchResultList).once();
    expect(mockSearchResult1.getFullName()).andReturn("TestSpace.Event1").once();
    expect(mockSearchResult2.getFullName()).andReturn("TestSpace.Event2").once();
    
    replayDefault();
    List<IEvent> eventApiList = searchResult.getEventList(offset, limit);
    verifyDefault();
    
    assertEquals(2, eventApiList.size());
    assertEquals(new DocumentReference("xwikidb", "TestSpace", "Event1"), 
        eventApiList.get(0).getDocumentReference());
    assertEquals(new DocumentReference("xwikidb", "TestSpace", "Event2"), 
        eventApiList.get(1).getDocumentReference());
    assertEquals(Arrays.asList(sortFields), Arrays.asList(sortFieldsCapture.getValue()));
  }
  
  @Test
  public void testGetSize() throws Exception {    
    String queryString = "theQuery";
    String[] sortFields = new String[] {"field1", "field2"};
    EventSearchResult searchResult = getEventSearchResult(queryString, sortFields, false);

    Capture<String[]> sortFieldsCapture = new Capture<String[]>();
    expect(lucenePluginMock.getSearchResults(eq(queryString), capture(sortFieldsCapture), 
        isNull(String.class), eq("default,de"), same(context))).andReturn(
        searchResultsMock).once();
    expect(searchResultsMock.getHitcount()).andReturn(2).once();
    
    replayDefault();
    int size = searchResult.getSize();
    verifyDefault();
    
    assertEquals(2, size);
    assertEquals(Arrays.asList(sortFields), Arrays.asList(sortFieldsCapture.getValue()));
  }

  private EventSearchResult getEventSearchResult(String queryString,
      String[] sortFields, boolean skipChecks) {
    EventSearchResult searchResult = new EventSearchResult(queryString, Arrays.asList(
        sortFields), skipChecks, context);
    searchResult.injectLucenePlugin(lucenePluginMock);
    return searchResult;
  }

}
