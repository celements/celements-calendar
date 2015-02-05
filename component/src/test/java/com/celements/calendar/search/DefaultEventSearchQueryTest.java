package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class DefaultEventSearchQueryTest extends AbstractBridgedComponentTestCase {

  @Test
  public void testGetAsLuceneQuery_noFuzzy() throws ParseException {
    String db = "myDB";
    IEventSearchQuery query = new DefaultEventSearchQuery(db);

    assertEquals(db, query.getDatabase());
    assertEquals(0, query.getSortFields().size());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    String db = "myDB";
    List<String> sortFields = Arrays.asList("field1", "field2");
    IEventSearchQuery query = new DefaultEventSearchQuery(db, sortFields);

    assertEquals(db, query.getDatabase());
    assertEquals(sortFields, query.getSortFields());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\"))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

}
