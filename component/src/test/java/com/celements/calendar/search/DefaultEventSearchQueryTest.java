package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class DefaultEventSearchQueryTest extends AbstractBridgedComponentTestCase {

  @Test
  public void testGetAsLuceneQuery_noFuzzy() throws ParseException {
    WikiReference wikiRef = new WikiReference("myDB");
    IEventSearchQuery query = new DefaultEventSearchQuery(wikiRef);

    assertEquals(wikiRef.getName(), query.getDatabase());
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(0, query.getSortFields().size());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    WikiReference wikiRef = new WikiReference("myDB");
    List<String> sortFields = Arrays.asList("field1", "field2");
    IEventSearchQuery query = new DefaultEventSearchQuery(wikiRef, sortFields);

    assertEquals(wikiRef.getName(), query.getDatabase());
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

}
