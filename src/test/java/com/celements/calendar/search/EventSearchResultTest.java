package com.celements.calendar.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;

public class EventSearchResultTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private LucenePlugin lucenePluginMock;
  private SearchResults searchResultsMock;
  EventSearchResult searchResult;

  @Before
  public void setUp_RulesFactoryTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    lucenePluginMock = createMock(LucenePlugin.class);
    searchResultsMock = createMock(SearchResults.class);
    context.setWiki(xwiki);
    searchResult = new EventSearchResult("", null, context);
    searchResult.injectLucenePlugin(lucenePluginMock);
  }
  
  @Test
  public void testGetEventList() throws Exception {
    int offset = 5;
    int limit = 10;
    
    SearchResult mockSearchResult1 = createMock(SearchResult.class);
    SearchResult mockSearchResult2 = createMock(SearchResult.class);
    List<SearchResult> searchResultList = new ArrayList<SearchResult>();
    searchResultList.add(mockSearchResult1);
    searchResultList.add(mockSearchResult2);
    
    expect(lucenePluginMock.getSearchResults(eq(""), eq((String[]) null), 
        eq((String) null), eq("default,de"), same(context))).andReturn(searchResultsMock
        ).once();
    expect(searchResultsMock.getResults(eq(offset + 1), eq(limit))).andReturn(
        searchResultList).once();
    expect(mockSearchResult1.getFullName()).andReturn("TestSpace.Event1").once();
    expect(mockSearchResult2.getFullName()).andReturn("TestSpace.Event2").once();
    
    replayAll(mockSearchResult1, mockSearchResult2);
    List<IEvent> eventApiList = searchResult.getEventList(offset, limit);
    verifyAll(mockSearchResult1, mockSearchResult2);
    
    assertEquals(2, eventApiList.size());
    assertEquals(new DocumentReference("xwikidb", "TestSpace", "Event1"), 
        eventApiList.get(0).getDocumentReference());
    assertEquals(new DocumentReference("xwikidb", "TestSpace", "Event2"), 
        eventApiList.get(1).getDocumentReference());
  }
  
  @Test
  public void testGetSize() throws Exception {    
    expect(lucenePluginMock.getSearchResults(eq(""), eq((String[]) null), 
        eq((String) null), eq("default,de"), same(context))).andReturn(searchResultsMock
        ).once();
    expect(searchResultsMock.getHitcount()).andReturn(2).once();
    
    replayAll();
    int size = searchResult.getSize();
    verifyAll();
    
    assertEquals(2, size);
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, lucenePluginMock, searchResultsMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, lucenePluginMock, searchResultsMock);
    verify(mocks);
  }

}
