package com.celements.calendar.search;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class CalendarEventSearchQueryTest extends AbstractBridgedComponentTestCase {
  
  private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
  
  @Test
  public void testNewCalendarEventSearchQuery() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, sortFields, skipChecks);
    assertEquals(database, query.getDatabase());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(skipChecks, query.skipChecks());
  }
  
  @Test
  public void testNewCalendarEventSearchQuery_defaultSortFields() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    boolean skipChecks = false;
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, null, skipChecks);
    assertEquals(database, query.getDatabase());
    assertDefaultSortFields(query.getSortFields(), isArchive);
    assertEquals(skipChecks, query.skipChecks());
  }
  
  @Test
  public void testNewCalendarEventSearchQuery_fromQuery() throws Exception {
    String database = "theDB";
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    IEventSearchQuery fromQuery = new DefaultEventSearchQuery(database, null, sortFields, 
        skipChecks);
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(fromQuery, startDate, 
        isArchive, lang, spaces);
    assertEquals(database, query.getDatabase());
    assertEquals(sortFields, query.getSortFields());
    assertEquals(skipChecks, query.skipChecks());
  }
  
  @Test
  public void testNewCalendarEventSearchQuery_fromQuery_defaultSortFields(
      ) throws Exception {
    String database = "theDB";
    boolean skipChecks = false;
    IEventSearchQuery fromQuery = new DefaultEventSearchQuery(database, null, null, 
        skipChecks);
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    CalendarEventSearchQuery query = new CalendarEventSearchQuery(fromQuery, startDate, 
        isArchive, lang, spaces);
    assertEquals(database, query.getDatabase());
    assertDefaultSortFields(query.getSortFields(), isArchive);
    assertEquals(skipChecks, query.skipChecks());
  }
  
  @Test
  public void testGetAsLuceneQuery() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, null, false);

    assertEquals(database, query.getDatabase());
    String expQueryString = "(wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
    assertFalse(query.skipChecks());
  }
  
  @Test
  public void testGetAsLuceneQuery_isArchive() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = true;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, null, false);

    assertEquals(database, query.getDatabase());
    String expQueryString = "(wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
    assertFalse(query.skipChecks());
  }
  
  @Test
  public void testGetAsLuceneQuery_sortFields() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
    List<String> sortFields = Arrays.asList("field1", "field2");
    boolean skipChecks = true;
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, sortFields, skipChecks);

    assertEquals(database, query.getDatabase());
    String expQueryString = "(wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertEquals(sortFields, query.getSortFields());
    assertTrue(query.skipChecks());
  }
  
  @Test
  public void testGetAsLuceneQuery_noSpaces() throws Exception {
    String database = "theDB";
    Date startDate = SDF.parse("201405090125");
    boolean isArchive = false;
    String lang = "de";
    List<String> spaces = Collections.emptyList();
    IEventSearchQuery query = new CalendarEventSearchQuery(database, startDate, isArchive, 
        lang, spaces, null, false);

    assertEquals(database, query.getDatabase());
    String expQueryString = "(wiki:(+\"theDB\") "
        + "AND object:(+\"Classes.CalendarEventClass\") "
        + "AND space:(+\"NullSpace\") "
        + "AND Classes.CalendarEventClass.lang:(+\"de\") "
        + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
    assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
    assertDefaultSortFields(query.getSortFields(), isArchive);
    assertFalse(query.skipChecks());
  }
  
  @Test
  public void testGetSortFields() {
    List<String> inSortFields = Arrays.asList("field1", "field2");
    assertEquals(inSortFields, CalendarEventSearchQuery.getDefaultSortFields(inSortFields, 
        false));
  }
  
  @Test
  public void testGetSortFields_default() {
    boolean inverted = false;
    List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(null, 
        inverted);
    assertDefaultSortFields(sortFields, inverted);
  }
  
  @Test
  public void testGetSortFields_default_inverted() {
    boolean inverted = true;
    List<String> inSortFields = Collections.emptyList();
    List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(inSortFields, 
        inverted);
    assertDefaultSortFields(sortFields, inverted);
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
