package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class EventSearchTest extends AbstractBridgedComponentTestCase {
  
  private EventSearch eventSearch;

  @Before
  public void setUp_EventSearchTest() throws Exception {
    eventSearch = (EventSearch) Utils.getComponent(IEventSearch.class);
  }

  @Test
  public void testGetSearchResult() throws Exception {
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    IEventSearchQuery query = new DefaultEventSearchQuery("theDB", sortFields, skipChecks);

    replayDefault();
    EventSearchResult searchResult = eventSearch.getSearchResult(query);
    verifyDefault();

    assertNotNull(searchResult);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") AND wiki:theDB";
    assertEquals(expQueryString, searchResult.getLuceneQuery());
    assertEquals(skipChecks, searchResult.getSkipChecks());
    assertEquals(sortFields, searchResult.getSortFields());
  }

  @Test
  public void testGetSearchResult_null() throws Exception {
    replayDefault();
    EventSearchResult searchResult = eventSearch.getSearchResult(null);
    verifyDefault();

    assertNotNull(searchResult);
    String expQueryString = "object:(+\"Classes.CalendarEventClass\") AND wiki:xwikidb";
    assertEquals(expQueryString, searchResult.getLuceneQuery());
    assertFalse(searchResult.getSkipChecks());
    assertEquals(0, searchResult.getSortFields().size());
  }

}
