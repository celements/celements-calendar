package com.celements.calendar.search;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.LuceneSearchResult;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class EventSearchServiceTest extends AbstractComponentTest {

  private EventSearchService eventSearchService;
  private ConfigurationSource configSourceMock;

  @Before
  public void setUp_EventSearchServiceTest() throws Exception {
    eventSearchService = (EventSearchService) Utils.getComponent(IEventSearchRole.class);
    configSourceMock = createDefaultMock(ConfigurationSource.class);
    eventSearchService.injectConfigSource(configSourceMock);
    LucenePlugin lucenePlugin = createDefaultMock(LucenePlugin.class);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(lucenePlugin).anyTimes();
    expect(lucenePlugin.getAnalyzer()).andReturn(null).anyTimes();
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
    IEventSearchQuery query = new DefaultEventSearchQuery(new WikiReference("thedb"),
        sortFields);
    expect(configSourceMock.getProperty(eq("calendar.search.skipChecks"),
        same(Boolean.class))).andReturn(false);
    replayDefault();
    EventSearchResult searchResult = eventSearchService.getSearchResult(query);
    LuceneSearchResult lSearchResult = searchResult.getSearchResult();
    verifyDefault();
    assertNotNull(searchResult);
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"thedb\") "
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
