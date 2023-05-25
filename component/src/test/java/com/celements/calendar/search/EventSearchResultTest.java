package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

public class EventSearchResultTest extends AbstractComponentTest {

  private ILuceneSearchService searchServiceMock;

  @Before
  public void setUp_EventSearchResultTest() throws Exception {
    searchServiceMock = createDefaultMock(ILuceneSearchService.class);
  }

  @Test
  public void testGetSearchResult() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    EventSearchResult result = getEventSearchResult(query, sortFields, false);
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages)))
        .andReturn(resultMock).once();
    replayDefault();
    LuceneSearchResult ret = result.getSearchResult();
    verifyDefault();
    assertSame(resultMock, ret);
  }

  @Test
  public void testGetSearchResult_skipChecks() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    EventSearchResult result = getEventSearchResult(query, sortFields, true);
    expect(searchServiceMock.searchWithoutChecks(same(query), eq(sortFields),
        eq(languages))).andReturn(resultMock).once();
    replayDefault();
    LuceneSearchResult ret = result.getSearchResult();
    verifyDefault();
    assertSame(resultMock, ret);
  }

  @Test
  public void testGetEventList() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    int offset = 5;
    int limit = 10;
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    DocumentReference docRef1 = new DocumentReference("xwikidb", "TestSpace", "Event1");
    DocumentReference docRef2 = new DocumentReference("xwikidb", "TestSpace", "Event2");
    AttachmentReference attRef = new AttachmentReference("fileName", docRef1);
    List<EntityReference> docRefList = Arrays.<EntityReference>asList(docRef1, docRef2,
        attRef);
    EventSearchResult result = getEventSearchResult(query, sortFields, false);
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages)))
        .andReturn(resultMock).once();
    expect(resultMock.getResults(eq(offset), eq(limit))).andReturn(docRefList).once();
    replayDefault();
    List<IEvent> ret = result.getEventList(offset, limit);
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(docRefList.get(0), ret.get(0).getDocumentReference());
    assertEquals(docRefList.get(1), ret.get(1).getDocumentReference());
  }

  @Test
  public void testGetEventList_LSE() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    int offset = 5;
    int limit = 10;
    Throwable cause = createDefaultMock(LuceneSearchException.class);
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    EventSearchResult result = getEventSearchResult(query, sortFields, false);
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages)))
        .andReturn(resultMock).once();
    expect(resultMock.getResults(eq(offset), eq(limit))).andThrow(cause).once();
    replayDefault();
    try {
      result.getEventList(offset, limit);
      fail("should throw LuceneSearchException");
    } catch (LuceneSearchException lse) {
      assertSame(cause, lse);
    }
    verifyDefault();
  }

  @Test
  public void testGetSize() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    EventSearchResult result = getEventSearchResult(query, sortFields, false);
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages)))
        .andReturn(resultMock).once();
    expect(resultMock.getSize()).andReturn(2).once();
    replayDefault();
    int ret = result.getSize();
    verifyDefault();
    assertEquals(2, ret);
  }

  @Test
  public void testGetSize_LSE() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    List<String> sortFields = Arrays.asList("field1", "field2");
    List<String> languages = null;
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    Throwable cause = createDefaultMock(LuceneSearchException.class);

    EventSearchResult result = getEventSearchResult(query, sortFields, false);
    expect(searchServiceMock.search(same(query), eq(sortFields), eq(languages)))
        .andReturn(resultMock).once();
    expect(resultMock.getSize()).andThrow(cause).once();

    replayDefault();
    try {
      result.getSize();
      fail("should throw LuceneSearchException");
    } catch (LuceneSearchException lse) {
      assertSame(cause, lse);
    }
    verifyDefault();
  }

  private EventSearchResult getEventSearchResult(LuceneQuery query,
      List<String> sortFields, boolean skipChecks) {
    EventSearchResult searchResult = new EventSearchResult(query, sortFields, skipChecks);
    searchResult.injectSearchService(searchServiceMock);
    return searchResult;
  }

}
