package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;

public class EventSearchQueryTest {
  
  private static final String SPACE = "space";
  private static final String TITLE = "Classes.CalendarEventClass.l_title";
  private static final String DESCR = "Classes.CalendarEventClass.l_description";
  
  private IQueryService queryServiceMock;
  
  @Before
  public void setUp_EventSearchResultTest() {
    queryServiceMock = createMock(IQueryService.class);
  }
  
  @Test
  public void testGetAsLuceneQuery() {
    String dbName = "myDB";
    String spaceName = "mySpace";
    String searchTerm = "some search term";
    EventSearchQuery query = new EventSearchQuery(spaceName, searchTerm);
    query.injectQueryService(queryServiceMock);
    
    expect(queryServiceMock.createQuery()).andReturn(new LuceneQueryApi(dbName)).once();
    expect(queryServiceMock.createRestriction(eq(SPACE), eq(spaceName), eq(true))
        ).andReturn(new LuceneQueryRestrictionApi(SPACE, spaceName, true)).once();
    expect(queryServiceMock.createRestriction(eq(TITLE), eq(searchTerm), eq(false))
        ).andReturn(new LuceneQueryRestrictionApi(TITLE, searchTerm, false)).once();
    expect(queryServiceMock.createRestriction(eq(DESCR), eq(searchTerm), eq(false))
        ).andReturn(new LuceneQueryRestrictionApi(DESCR, searchTerm, false)).once();
    
    replayAll();
    LuceneQueryApi luceneQuery = query.getAsLuceneQuery();
    verifyAll();
    
    String compareQueryString = "space:(+mySpace*) AND " +
    		"(Classes.CalendarEventClass.l_title:(some~ search~ term~) OR " +
    		"Classes.CalendarEventClass.l_description:(some~ search~ term~)) " +
    		"AND wiki:myDB";
    assertEquals(compareQueryString, luceneQuery.getQueryString());
  }

  private void replayAll(Object ... mocks) {
    replay(queryServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(queryServiceMock);
    verify(mocks);
  }

}
