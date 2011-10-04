/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.calendar;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class EventTest extends AbstractBridgedComponentTestCase {

  private Event event;
  private XWikiContext context;
  private ICalendar calendar;
  private TestMessageTool testMsgTool;
  private XWiki xwiki;

  @Before
  public void setUp_EventTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    testMsgTool = (TestMessageTool)context.getMessageTool();
    testMsgTool.injectMessage("cel_cal_datetime_delim", " - ");
    List<BaseObject> objList = new ArrayList<BaseObject>();
    BaseObject eventObj = new BaseObject();
    eventObj.setName("Test.Event1");
    eventObj.setStringValue("lang", "de");
    objList.add(eventObj);
    event = new Event(objList, "TestSpace", context);
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    calendar = createMock(ICalendar.class);
    event.internal_setCalendar(calendar);
  }

  @Test
  public void testEvent_NPE() {
    //getObjects may return null!!
    Event myEvent = new Event(null, "tipps", context);
    assertNotNull(myEvent);
  }

  @Test
  public void testGetObj_NPE() {
    //getObjects may return null!!
    XWikiDocument eventDoc = new XWikiDocument();
    eventDoc.setFullName("MySpace.MyCal");
    //getObjects may return null!!
    replay(xwiki);
    Event myEvent = new Event(eventDoc, context);
    myEvent.internal_setDefaultLanguage("de");
    verify(xwiki);
    assertNull(myEvent.getObj("de"));
  }

  @Test
  public void testgetDocName_NPE() {
    Event myEvent = new Event(null, "tipps", context);
    assertNotNull(myEvent.getEventObjMap());
    assertNotNull(myEvent.getDocName());
    assertEquals("", myEvent.getDocName());
  }

  @Test
  public void testSplitLanguageDependentFields_empty() {
    List<String> propertyNames = new ArrayList<String>();
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty indepList.", confIndep.isEmpty());
    assertTrue("Expecting empty depList.", confDep.isEmpty());
  }

  @Test
  public void testSplitLanguageDependentFields_detaillink() {
    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("detaillink");
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty indepList.", confIndep.isEmpty());
    assertTrue("Expecting empty depList.", confDep.isEmpty());
  }

  @Test
  public void testSplitLanguageDependentFields_date() {
    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("date");
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty depList.", confDep.isEmpty());
    assertTrue("Expecting indepList containing eventDate.",
        confIndep.contains("eventDate"));
    assertTrue("Expecting indepList containing only eventDate.",
        confIndep.size() == 1);
  }

  @Test
  public void testSplitLanguageDependentFields_time() {
    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("time");
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty depList.", confDep.isEmpty());
    assertTrue("Expecting indepList containing eventDate.",
        confIndep.contains("eventDate"));
    assertTrue("Expecting indepList containing only eventDate.",
        confIndep.size() == 1);
  }

  @Test
  public void testSplitLanguageDependentFields_date_end() {
    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("date_end");
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty depList.", confDep.isEmpty());
    assertTrue("Expecting indepList containing eventDate_end.",
        confIndep.contains("eventDate_end"));
    assertTrue("Expecting indepList containing only eventDate_end.",
        confIndep.size() == 1);
  }

  @Test
  public void testSplitLanguageDependentFields_time_end() {
    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("time_end");
    Set<String> confDep = new HashSet<String>();
    Set<String> confIndep = new HashSet<String>();
    event.splitLanguageDependentFields(confIndep, confDep, propertyNames);
    assertTrue("Expecting empty depList.", confDep.isEmpty());
    assertTrue("Expecting indepList containing eventDate_end.",
        confIndep.contains("eventDate_end"));
    assertTrue("Expecting indepList containing only eventDate_end.",
        confIndep.size() == 1);
  }

  @Test
  public void testDisplayField_two_Dates_sameDay() throws ParseException {
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 15:30:00"));
    String displayPart = event.displayField("date-date_end.", context);
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>", displayPart);
  }

  @Test
  public void testDisplayField_two_Dates_differentDays() throws ParseException {
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    String displayPart = event.displayField("date-date_end", context);
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>"
        + "<span class=\"cel_cal_date_end\"> - 28.04.2009</span>", displayPart);
  }

  @Test
  public void testDisplayField_startDate_only() throws ParseException {
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", null);
    String displayPart = event.displayField("date-date_end", context);
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>", displayPart);
  }

  @Test
  public void testInternalDisplayField_two_Dates_differentDays (
      ) throws ParseException {
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    String displayPart = event.internalDisplayField("date-date_end", false,
        context);
    assertEquals("27.04.2009 - 28.04.2009", displayPart);
  }

  @Test
  public void testGetNonEmptyFields_CombinedFields (
      ) throws ParseException {
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    replay(calendar);
    List<String> resultList = event.getNonEmptyFields(fieldList, context);
    assertEquals("getNonEmptyFields must support combined fields",
        fieldList, resultList);
    verify(calendar);
  }

  @Test
  public void testGetNonEmptyFields_optionalDateTime (
      ) throws ParseException {
    event.internal_setEventDoc(new XWikiDocument());
    event.internal_setDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 00:00:00"));
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("time.-time_end.");
    fieldList.add("date_end.");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    replay(calendar);
    List<String> resultList = event.getNonEmptyFields(fieldList, context);
    assertEquals("getNonEmptyFields must support optional time fields",
        Collections.emptyList(), resultList);
    verify(calendar);
  }

  @Test
  public void testNeedsMoreLink_multiple_used_fields (
      ) throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    eventOjb.setLargeStringValue("contact_rte", "Gerbergasse 30");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date");
    fieldList.add("date_end");
    fieldList.add("date");
    fieldList.add("l_title");
    List<String> overviewFieldList = new ArrayList<String>();
    overviewFieldList.add("date");
    overviewFieldList.add("date_end");
    overviewFieldList.add("l_title");
    overviewFieldList.add("detaillink");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    expect(calendar.getOverviewFields()).andStubReturn(overviewFieldList);
    replay(calendar);
    boolean needsMoreLink = event.needsMoreLink(context);
    verify(calendar);
    assertFalse("needsMoreLink must correctly handle multiple occurences.",
        needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_combinedFields (
      ) throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    eventOjb.setLargeStringValue("contact_rte", "Gerbergasse 30");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end");
    fieldList.add("contact_rte-l_title");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    List<String> emptylist = Collections.emptyList();
    expect(calendar.getOverviewFields()).andStubReturn(emptylist);
    //All additional fields in detailview are combined fields. If not correct
    //handled all fields will be emtpy and needsMoreLink returns wrongly false.
    replay(calendar);
    boolean needsMoreLink = event.needsMoreLink(context);
    verify(calendar);
    assertTrue("needsMoreLink must support compbined fields.",
        needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_identical_detailAndOvervFields_with_combinedFields (
      ) throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end");
    fieldList.add("l_title_l_description");
    fieldList.add("location_rte");
    List<String> overviewFieldList = new ArrayList<String>();
    overviewFieldList.add("date-date_end-location_rte");
    overviewFieldList.add("time-time_end");
    overviewFieldList.add("l_title-l_description");
    overviewFieldList.add("detaillink");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    expect(calendar.getOverviewFields()).andStubReturn(overviewFieldList);
    replay(calendar);
    boolean needsMoreLink = event.needsMoreLink(context);
    verify(calendar);
    assertFalse("needsMoreLink must look at displayed property names.",
        needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_identical_optinalTimeEnd_in_detail (
      ) throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end.");
    fieldList.add("l_title_l_description");
    fieldList.add("location_rte");
    List<String> overviewFieldList = new ArrayList<String>();
    overviewFieldList.add("date-time-date_end-location_rte");
    overviewFieldList.add("l_title-l_description");
    overviewFieldList.add("detaillink");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    expect(calendar.getOverviewFields()).andStubReturn(overviewFieldList);
    replay(calendar);
    boolean needsMoreLink = event.needsMoreLink(context);
    verify(calendar);
    assertFalse("needsMoreLink must handle empty optional time_end field.",
        needsMoreLink);
  }
  
  @Test
  public void testGetDisplayPart_Empty_time_And_timeEnd() throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end");
    fieldList.add("l_title_l_description");
    fieldList.add("location_rte");
    List<String> overviewFieldList = new ArrayList<String>();
    overviewFieldList.add("date-date_end-location_rte");
    overviewFieldList.add("time-time_end");
    overviewFieldList.add("l_title-l_description");
    overviewFieldList.add("detaillink");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    expect(calendar.getOverviewFields()).andStubReturn(overviewFieldList);
    replay(calendar);
    String displayTimePart = event.getDisplayPart("time", true, context);
    String displayTimeEndPart = event.getDisplayPart("time_end", true, context);
    verify(calendar);
    assertEquals("Expecting empty String for optional time (00:00)",
        "", displayTimePart);
    assertEquals("Expecting empty String for optional time_end (00:00)",
        "", displayTimeEndPart);
  }

  @Test
  public void testInternalDisplayField_optional_time() throws ParseException {
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end");
    fieldList.add("l_title_l_description");
    fieldList.add("location_rte");
    List<String> overviewFieldList = new ArrayList<String>();
    overviewFieldList.add("date-date_end-location_rte");
    overviewFieldList.add("time-time_end");
    overviewFieldList.add("l_title-l_description");
    overviewFieldList.add("detaillink");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    expect(calendar.getOverviewFields()).andStubReturn(overviewFieldList);
    replay(calendar);
    String displayTimePart = event.internalDisplayField("time.", false,
        context);
    String displayTimeEndPart = event.internalDisplayField("time_end.", false,
        context);
    verify(calendar);
    assertEquals("Expecting empty String for optional time (00:00)",
        "", displayTimePart);
    assertEquals("Expecting empty String for optional time_end (00:00)",
        "", displayTimeEndPart);
  }

  @Test
  public void testGetDetailConfigForField() {
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time.-time_end.");
    fieldList.add("l_title_l_description");
    fieldList.add("location_rte");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    replay(calendar);
    assertEquals("time_end.", event.getDetailConfigForField("time_end", context));
    assertEquals("time.", event.getDetailConfigForField("time", context));
    verify(calendar);
  }
  
  @Test
  public void testIsOptionalField() {
    assertTrue(event.isIncludingFieldAsOptional("time_end", "time.-time_end."));
    assertTrue(event.isIncludingFieldAsOptional("time", "time.-time_end."));
    assertFalse(event.isIncludingFieldAsOptional("time_end", "time-time_end"));
    assertFalse(event.isIncludingFieldAsOptional("time", "time-time_end"));
    assertFalse(event.isIncludingFieldAsOptional("time", "date-date_end"));
  }

}
