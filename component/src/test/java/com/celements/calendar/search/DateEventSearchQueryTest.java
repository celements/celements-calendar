package com.celements.calendar.search;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;

public class DateEventSearchQueryTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  @Before
  public void setup_DateEventSearchQueryTest() {

  }

  @Test
  public void test_getWikiRef_protect_WikiReference() throws Exception {
    String database = "myDB";
    WikiReference wikiRef = new WikiReference(database);
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    replayDefault();
    IDateEventSearchQuery query = new DateEventSearchQuery(wikiRef, fromDate, toDate, null);
    assertEquals("returned wikiRef must", database, query.getWikiRef().getName());
    wikiRef = new WikiReference("testName");
    assertEquals("getWikiRef must return given database wiki name", database,
        query.getWikiRef().getName());
    assertNotSame(wikiRef, query.getWikiRef());
    assertNotSame("getWikiRef must preserve the internal wiki reference against"
        + " external manipulation", query.getWikiRef(), query.getWikiRef());
    verifyDefault();
  }

  @Test
  public void test_getAsLuceneQuery() throws Exception {
    WikiReference wikiRef = new WikiReference("myDB");
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    IDateEventSearchQuery query = new DateEventSearchQuery(wikiRef, fromDate, toDate, null);
    replayDefault();
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(0, query.getSortFields().size());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

  @Test
  public void test_getAsLuceneQuery_withSortField() throws Exception {
    WikiReference wikiRef = new WikiReference("myDB");
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    List<String> sortFields = Arrays.asList("field1", "field2");
    IDateEventSearchQuery query = new DateEventSearchQuery(wikiRef, fromDate, toDate,
        sortFields);
    replayDefault();
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"myDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

}
