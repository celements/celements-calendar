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

public class DateEventSearchQueryTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  @Test
  public void testGetAsLuceneQuery() throws ParseException {
    String db = "myDB";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    DateEventSearchQuery query = new DateEventSearchQuery(db, fromDate, toDate, null, 
        false);

    assertEquals(db, query.getDatabase());
    assertEquals(0, query.getSortFields().size());
    assertFalse(query.skipChecks());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = 
        "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND wiki:myDB";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    String db = "myDB";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    DateEventSearchQuery query = new DateEventSearchQuery(db, fromDate, toDate, 
        sortFields, skipChecks);

    assertEquals(db, query.getDatabase());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(skipChecks, query.skipChecks());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = 
        "object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND wiki:myDB";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

}