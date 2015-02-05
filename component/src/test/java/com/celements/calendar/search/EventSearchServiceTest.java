package com.celements.calendar.search;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.LuceneSearchResult;
import com.xpn.xwiki.web.Utils;

public class EventSearchServiceTest extends AbstractBridgedComponentTestCase {
  
  private EventSearchService eventSearchService;
  private ConfigurationSource configSourceMock;

  @Before
  public void setUp_EventSearchServiceTest() throws Exception {
    eventSearchService = (EventSearchService) Utils.getComponent(IEventSearchRole.class);
    configSourceMock = createMockAndAddToDefault(ConfigurationSource.class);
    eventSearchService.injectConfigSource(configSourceMock);
  }

  @Test
  public void testSkipChecks_false() {
    expect(configSourceMock.getProperty(eq("calendar.search.skipChecks"), 
        same(Boolean.class))).andReturn(false);
    
    replayDefault();
    boolean ret = eventSearchService.skipChecks();
    verifyDefault();
    
    assertFalse(ret);
  }
  
  @Test
  public void testSkipChecks_true() {
    expect(configSourceMock.getProperty(eq("calendar.search.skipChecks"), 
        same(Boolean.class))).andReturn(true);
    
    replayDefault();
    boolean ret = eventSearchService.skipChecks();
    verifyDefault();
    
    assertTrue(ret);
  }

  @Test
  public void testGetSearchResult() throws Exception {
    List<String> sortFields = Arrays.asList("field1", "field2");
    IEventSearchQuery query = new DefaultEventSearchQuery("theDB", sortFields);
    expect(configSourceMock.getProperty(eq("calendar.search.skipChecks"), 
        same(Boolean.class))).andReturn(false);

    replayDefault();
    EventSearchResult searchResult = eventSearchService.getSearchResult(query);
    LuceneSearchResult lSearchResult = searchResult.getSearchResult();
    verifyDefault();

    assertNotNull(searchResult);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(expQueryString, lSearchResult.getQueryString());
    assertEquals(sortFields, lSearchResult.getSortFields());
  }

  @Test
  public void testGetSearchResult_null() throws Exception {
    expect(configSourceMock.getProperty(eq("calendar.search.skipChecks"), 
        same(Boolean.class))).andReturn(true);
    
    replayDefault();
    EventSearchResult searchResult = eventSearchService.getSearchResult(null);
    LuceneSearchResult lSearchResult = searchResult.getSearchResult();
    verifyDefault();

    assertNotNull(searchResult);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"xwikidb\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(expQueryString, lSearchResult.getQueryString());
    assertEquals(0, lSearchResult.getSortFields().size());
  }

}
