package com.celements.calendar.search;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.common.test.AbstractComponentTest;

public class CalendarEventSearchQueryTest extends AbstractComponentTest {

  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

  private ICalendar calMock;
  private WikiReference wikiRef;

  @Before
  public void setUp_CalendarEventSearchQueryTest() {
    calMock = createMockAndAddToDefault(ICalendar.class);
    wikiRef = new WikiReference("theDB");
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "someSpace", "someCal");
    expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(calMock.getWikiRef()).andReturn(wikiRef).anyTimes();
  }

  @Test
  public void test_newCalendarEventSearchQuery_wikiRef() throws Exception {
    List<String> sortFields = Arrays.asList("field1", "field2");

    replayDefault();
    IEventSearchQuery queryBuilder = new CalendarEventSearchQuery(wikiRef);
    queryBuilder.setSortFields(sortFields);
    verifyDefault();

    assertEquals(wikiRef, queryBuilder.getWikiRef());
    assertEquals(sortFields, queryBuilder.getSortFields());
  }

  @Test
  public void test_newCalendarEventSearchQuery() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Arrays.asList("field1", "field2");

    replayDefault();
    IEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_newCalendarEventSearchQuery_defaultSortFields() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Collections.emptyList();

    replayDefault();
    IEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    assertDefaultSortFields(query.getSortFields(), isArchive);
  }

  @Test
  public void test_newCalendarEventSearchQuery_fromQuery() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Arrays.asList("field1", "field2");

    replayDefault();
    IEventSearchQuery fromQuery = new DefaultEventSearchQuery(wikiRef, null, sortFields);
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, fromQuery);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_newCalendarEventSearchQuery_fromQuery_defaultSortFields() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Collections.emptyList();

    replayDefault();
    IEventSearchQuery fromQuery = new DefaultEventSearchQuery(wikiRef, null, sortFields);
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, fromQuery);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    assertDefaultSortFields(query.getSortFields(), isArchive);
  }

  @Test
  public void test_addCalendarRestrictions() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);

    replayDefault();
    ICalendarSearchQueryBuilder queryBuilder = new CalendarEventSearchQuery(wikiRef);
    queryBuilder.addCalendarRestrictions(calMock);

    assertEquals(wikiRef, queryBuilder.getWikiRef());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, queryBuilder.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(queryBuilder.getSortFields(), isArchive);
    verifyDefault();
  }

  @Test
  public void test_addCalendarRestrictions_sortFields() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Arrays.asList("field1", "field2");

    replayDefault();
    IEventSearchQuery fromQuery = new DefaultEventSearchQuery(wikiRef, null, sortFields);
    ICalendarSearchQueryBuilder queryBuilder = new CalendarEventSearchQuery(fromQuery);
    queryBuilder.addCalendarRestrictions(calMock);

    assertEquals(wikiRef, queryBuilder.getWikiRef());
    assertEquals(sortFields, queryBuilder.getSortFields());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, queryBuilder.getAsLuceneQuery().getQueryString());
    verifyDefault();
  }

  @Test
  public void test_getAsLuceneQuery() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Collections.emptyList();

    replayDefault();
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
  }

  @Test
  public void test_getAsLuceneQuery_isArchive() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Collections.emptyList();

    replayDefault();
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
  }

  @Test
  public void test_getAsLuceneQuery_sortFields() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Arrays.asList("field1", "field2");

    replayDefault();
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertEquals(sortFields, query.getSortFields());
  }

  @Test
  public void test_getAsLuceneQuery_noSpaces() throws Exception {
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    List<String> spaces = Collections.emptyList();
    expectForCalMock(startDate, isArchive, spaces);
    List<String> sortFields = Collections.emptyList();

    replayDefault();
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
    verifyDefault();

    assertEquals(wikiRef, query.getWikiRef());
    String expQueryString = "(type:(+\"wikipage\") AND wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") " + "AND space:(+\".\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
  }

  @Test
  public void test_getSortFields() {
    List<String> inSortFields = Arrays.asList("field1", "field2");
    assertEquals(inSortFields, CalendarEventSearchQuery.getDefaultSortFields(inSortFields, false));
  }

  @Test
  public void test_getSortFields_default() {
    boolean inverted = false;
    List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(null, inverted);
    assertDefaultSortFields(sortFields, inverted);
  }

  @Test
  public void test_getSortFields_default_inverted() {
    boolean inverted = true;
    List<String> inSortFields = Collections.emptyList();
    List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(inSortFields, inverted);
    assertDefaultSortFields(sortFields, inverted);
  }

  private void expectForCalMock(Date startDate, boolean isArchive, List<String> spaces) {
    expect(calMock.getStartDate()).andReturn(startDate).once();
    expect(calMock.isArchive()).andReturn(isArchive).times(2);
    expect(calMock.getAllowedSpaces()).andReturn(spaces).once();
  }

  private void assertDefaultSortFields(List<String> sortFields, boolean inverted) {
    String pref = inverted ? "-" : "";
    assertNotNull(sortFields);
    assertEquals(3, sortFields.size());
    assertEquals(pref + "Classes.CalendarEventClass.eventDate", sortFields.get(0));
    assertEquals(pref + "Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
    assertEquals(pref + "Classes.CalendarEventClass.l_title", sortFields.get(2));
  }

}
