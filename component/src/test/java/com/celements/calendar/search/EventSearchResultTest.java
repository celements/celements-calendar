package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;

public class EventSearchResultTest extends AbstractBridgedComponentTestCase {

  private ILuceneSearchService searchServiceMock;

  @Before
  public void setUp_EventSearchResultTest() throws Exception {
    searchServiceMock = createMockAndAddToDefault(ILuceneSearchService.class);
  }
  
  @Test
  public void testGetSearchResult() throws Exception {
    LuceneQuery query = new LuceneQuery("db");
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages))
        ).andReturn(resultMock).once();
    
    replayDefault();
    LuceneSearchResult ret = getEventSearchResult(query, sortFields, false
        ).getSearchResult();
    verifyDefault();
    
    assertSame(resultMock, ret);
  }
  
  @Test
  public void testGetSearchResult_skipChecks() throws Exception {
    LuceneQuery query = new LuceneQuery("db");
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    
    expect(searchServiceMock.searchWithoutChecks(same(query), eq(sortFields), 
        eq(languages))).andReturn(resultMock).once();
    
    replayDefault();
    LuceneSearchResult ret = getEventSearchResult(query, sortFields, true
        ).getSearchResult();
    verifyDefault();
    
    assertSame(resultMock, ret);
  }
  
  @Test
  public void testGetEventList() throws Exception {
    LuceneQuery query = new LuceneQuery("db");
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    int offset = 5;
    int limit = 10;
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    List<DocumentReference> docRefList = Arrays.asList(new DocumentReference("xwikidb", 
        "TestSpace", "Event1"), new DocumentReference("xwikidb", "TestSpace", "Event2"));
    
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages))
        ).andReturn(resultMock).once();
    expect(resultMock.getResults(eq(offset), eq(limit))).andReturn(docRefList).once();
    
    replayDefault();
    List<IEvent> ret = getEventSearchResult(query, sortFields, false).getEventList(
        offset, limit);
    verifyDefault();
    
    assertEquals(2, ret.size());
    assertEquals(docRefList.get(0), ret.get(0).getDocumentReference());
    assertEquals(docRefList.get(1), ret.get(1).getDocumentReference());
  }
  
  @Test
  public void testGetSize() throws Exception {
    LuceneQuery query = new LuceneQuery("db");
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages))
        ).andReturn(resultMock).once();
    expect(resultMock.getSize()).andReturn(2).once();
    
    replayDefault();
    int ret = getEventSearchResult(query, sortFields, false).getSize();
    verifyDefault();
    
    assertEquals(2, ret);
  }

  private EventSearchResult getEventSearchResult(LuceneQuery query, 
      List<String> sortFields, boolean skipChecks) {
    EventSearchResult searchResult = new EventSearchResult(query, sortFields, skipChecks);
    searchResult.injectSearchService(searchServiceMock);
    return searchResult;
  }

}
