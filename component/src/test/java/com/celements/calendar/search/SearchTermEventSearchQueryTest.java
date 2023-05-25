package com.celements.calendar.search;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

public class SearchTermEventSearchQueryTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  @Before
  public void prepare() {
    LucenePlugin lucenePlugin = createDefaultMock(LucenePlugin.class);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(lucenePlugin).anyTimes();
    expect(lucenePlugin.getAnalyzer()).andReturn(null).anyTimes();
  }

  @Test
  public void testGetAsLuceneQuery_noFuzzy() throws ParseException {
    WikiReference wikiRef = new WikiReference("mydb");
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = false;
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(wikiRef, fromDate,
        toDate, searchTerm, fuzzy, null);
    replayDefault();
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(0, query.getSortFields().size());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertFalse(query.isFuzzy());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"mydb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:(+some* +search* +term*) "
        + "OR Classes.CalendarEventClass.l_description:(+some* +search* +term*)))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

  @Test
  public void testGetAsLuceneQuery_fuzzy() throws ParseException {
    WikiReference wikiRef = new WikiReference("mydb");
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = true;
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(wikiRef, fromDate,
        toDate, searchTerm, fuzzy, null);
    replayDefault();
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(0, query.getSortFields().size());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertTrue(query.isFuzzy());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"mydb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:((some* OR some~) "
        + "AND (search* OR search~) AND (term* OR term~)) "
        + "OR Classes.CalendarEventClass.l_description:((some* OR some~) "
        + "AND (search* OR search~) AND (term* OR term~))))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

  @Test
  public void testGetAsLuceneQuery_withSortField() throws ParseException {
    WikiReference wikiRef = new WikiReference("mydb");
    String searchTerm = "some search term";
    Date fromDate = SDF.parse("200001010000");
    Date toDate = SDF.parse("201405090125");
    boolean fuzzy = false;
    List<String> sortFields = Arrays.asList("field1", "field2");
    SearchTermEventSearchQuery query = new SearchTermEventSearchQuery(wikiRef, fromDate,
        toDate, searchTerm, fuzzy, sortFields);
    replayDefault();
    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(fromDate, query.getFromDate());
    assertEquals(toDate, query.getToDate());
    assertEquals(searchTerm, query.getSearchTerm());
    assertFalse(query.isFuzzy());
    String compareQueryString = "(type:(+\"wikipage\") AND wiki:(+\"mydb\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND Classes.CalendarEventClass.eventDate:([200001010000 TO 201405090125]) "
        + "AND (Classes.CalendarEventClass.l_title:(+some* +search* +term*) "
        + "OR Classes.CalendarEventClass.l_description:(+some* +search* +term*)))";
    assertEquals(compareQueryString, query.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

}
