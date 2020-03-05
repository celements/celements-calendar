package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class DateEventSearchQueryTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  @Test
  public void testGetAsLuceneQuery() throws ParseException {
    WikiReference wikiRef = new WikiReference("myDB");
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    IDateEventSearchQuery query = new DateEventSearchQuery(wikiRef, fromDate, toDate, null);

    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(0, query.getSortFields().size());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    WikiReference wikiRef = new WikiReference("myDB");
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    List<String> sortFields = Arrays.asList("field1", "field2");
    IDateEventSearchQuery query = new DateEventSearchQuery(wikiRef, fromDate, toDate, 
        sortFields);

    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
  }

}
