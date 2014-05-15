package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class SearchTermEventSearchQueryTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  @Test
  public void testGetAsLuceneQuery_noFuzzy() throws ParseException {
    String db = "myDB";
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = false;
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(db, fromDate, 
        toDate, searchTerm, fuzzy, null, false);

    assertEquals(db, query.getDatabase());
    assertEquals(0, query.getSortFields().size());
    assertFalse(query.skipChecks());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertFalse(query.isFuzzy());
    String compareQueryString = 
        "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:(+some* +search* +term*) "
        + "OR Classes.CalendarEventClass.l_description:(+some* +search* +term*)) "
        + "AND wiki:myDB";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_fuzzy() throws ParseException {
    String db = "myDB";
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = true;
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(db, fromDate, 
        toDate, searchTerm, fuzzy, null, false);

    assertEquals(db, query.getDatabase());
    assertEquals(0, query.getSortFields().size());
    assertFalse(query.skipChecks());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertTrue(query.isFuzzy());
    String compareQueryString = 
        "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:((some* OR some~) "
        + "AND (search* OR search~) AND (term* OR term~)) "
        + "OR Classes.CalendarEventClass.l_description:((some* OR some~) "
        + "AND (search* OR search~) AND (term* OR term~))) AND wiki:myDB";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    String db = "myDB";
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = false;
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(db, fromDate, 
        toDate, searchTerm, fuzzy, sortFields, skipChecks);

    assertEquals(db, query.getDatabase());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(skipChecks, query.skipChecks());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertFalse(query.isFuzzy());
    String compareQueryString = 
        "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:(+some* +search* +term*) "
        + "OR Classes.CalendarEventClass.l_description:(+some* +search* +term*)) "
        + "AND wiki:myDB";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

}
