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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class EventTest extends AbstractBridgedComponentTestCase {

  private Event event;
  private XWikiContext context;
  private ICalendar calendar;
  private TestMessageTool testMsgTool;
  private XWiki xwiki;
  private DocumentReference eventDocRef;

  @Before
  public void setUp_EventTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    testMsgTool = (TestMessageTool)context.getMessageTool();
    testMsgTool.injectMessage("cel_cal_datetime_delim", " - ");
    List<BaseObject> objList = new ArrayList<BaseObject>();
    BaseObject eventObj = new BaseObject();
    eventDocRef = new DocumentReference(context.getDatabase(), "Test", "Event1");
    eventObj.setDocumentReference(eventDocRef);
    eventObj.setStringValue("lang", "de");
    objList.add(eventObj);
    event = new Event(objList);
    event.injectDocumentReference(eventDocRef);
    event.injectDefaultLanguage("de");
    calendar = createMockAndAddToDefault(ICalendar.class);
    event.injectCalendar(calendar);
  }

  @Test
  @Deprecated
  public void testEvent_NPE() {
    Event myEvent = new Event(null, "tipps", context);
    assertNotNull(myEvent);
  }

  @Test
  public void testGetObj_NPE() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc
        ).atLeastOnce();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    Event myEvent = new Event(myEventDocRef);
    myEvent.injectDefaultLanguage("de");
    assertNull(myEvent.getObj("de"));
    verifyDefault();
  }

  @Test
  public void testGetObj_langPrio() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    Event myEvent = new Event(myEventDocRef);
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    BaseObject objEn = new BaseObject();
    objEn.setStringValue(ICalendarClassConfig.PROPERTY_LANG, "en");
    objEn.setXClassReference(myEvent.getCalendarEventClassRef());
    eventDoc.addXObject(objEn);
    BaseObject objNonEn = new BaseObject();
    objNonEn.setXClassReference(myEvent.getCalendarEventClassRef());
    eventDoc.addXObject(objNonEn);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc
        ).atLeastOnce();
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    myEvent.injectDefaultLanguage("de");
    assertSame("Expecting to get en obj and not default one", objEn, myEvent.getObj("en"));
    verifyDefault();
  }

  @Test
  public void testGetObj_noRequest() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc).once();
    replayDefault();
    Event myEvent = new Event(myEventDocRef);
    myEvent.injectDefaultLanguage("de");
    assertNull(myEvent.getObj("de"));
    verifyDefault();
  }

  @Test
  public void testGetObj_withRequestTemplate() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    Event myEvent = new Event(myEventDocRef);   
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc).once();
    DocumentReference tmplDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyTemplate");
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get(eq("template"))).andReturn(Utils.getComponent(
        IWebUtilsService.class).serializeRef(tmplDocRef)).once();    
    expect(getWikiMock().exists(eq(tmplDocRef), same(getContext()))).andReturn(true
        ).once();
    expect(getWikiMock().exists(eq(myEventDocRef), same(getContext()))).andReturn(false
        ).once();
    
    replayDefault(); 
    myEvent.injectDefaultLanguage("de");
    BaseObject obj = myEvent.getObj("de");
    assertNotNull(obj);
    assertEquals(myEvent.getCalendarEventClassRef(), obj.getXClassReference());
    verifyDefault();
  }

  @Test
  public void testGetObj_withRequestTemplate_docExists() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    Event myEvent = new Event(myEventDocRef);   
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc).once();
    DocumentReference tmplDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyTemplate");
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get(eq("template"))).andReturn(Utils.getComponent(
        IWebUtilsService.class).serializeRef(tmplDocRef)).once();    
    expect(getWikiMock().exists(eq(tmplDocRef), same(getContext()))).andReturn(true
        ).once();
    expect(getWikiMock().exists(eq(myEventDocRef), same(getContext()))).andReturn(true
        ).once();
    
    replayDefault(); 
    myEvent.injectDefaultLanguage("de");
    assertNull(myEvent.getObj("de"));
    verifyDefault();
  }

  @Test
  public void testGetObj_withRequestTemplate_notExists() throws Exception {
    DocumentReference myEventDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyCal");
    Event myEvent = new Event(myEventDocRef);   
    XWikiDocument eventDoc = new XWikiDocument(myEventDocRef);
    expect(xwiki.getDocument(eq(myEventDocRef), same(context))).andReturn(eventDoc).once();
    DocumentReference tmplDocRef = new DocumentReference(context.getDatabase(), 
        "MySpace", "MyTemplate");
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get(eq("template"))).andReturn(Utils.getComponent(
        IWebUtilsService.class).serializeRef(tmplDocRef)).once();    
    expect(getWikiMock().exists(eq(tmplDocRef), same(getContext()))).andReturn(false
        ).once();
    
    replayDefault(); 
    myEvent.injectDefaultLanguage("de");
    assertNull(myEvent.getObj("de"));
    verifyDefault();
  }

  @Test
  public void testGetEventDocument() throws Exception {
    expect(xwiki.getDocument(eq(eventDocRef), same(context))).andReturn(new XWikiDocument(
        eventDocRef)).atLeastOnce();
    replayDefault();
    assertNotNull(event.getEventDocument());
    assertEquals(eventDocRef, event.getEventDocument().getDocumentReference());
    assertSame(event.getEventDocument(), event.getEventDocument());
    verifyDefault();
  }

  @Test
  public void testNewEvent_docRef() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "myEvent");
    XWikiDocument eventDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(eventDoc).anyTimes();
    replayDefault();
    Event myEvent = new Event(docRef);
    assertNotNull(myEvent.getEventObjMap());
    assertNotNull(myEvent.getDocumentReference());
    assertEquals(docRef, myEvent.getDocumentReference());
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testNewEvent_docRef_context() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "myEvent");
    XWikiDocument eventDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(eventDoc).anyTimes();
    replayDefault();
    Event myEvent = new Event(docRef, context);
    assertNotNull(myEvent.getEventObjMap());
    assertNotNull(myEvent.getDocName());
    assertEquals("", myEvent.getDocName());
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetEventDocument_badInit_noObjects() {
    Event myEvent = new Event(null, "tipps", context);
    assertNull(myEvent.getEventDocument());
  }

  @Test
  @Deprecated
  public void testgetDocName_NPE() {
    Event myEvent = new Event(null, "tipps", context);
    assertNotNull(myEvent.getEventObjMap());
    assertNotNull(myEvent.getDocName());
    assertEquals("", myEvent.getDocName());
  }

  @Test
  @Deprecated
  public void testgetDocumentReference_NPE() {
    Event myEvent = new Event(null, "tipps", context);
    assertNotNull(myEvent.getEventObjMap());
    assertNull(myEvent.getDocumentReference());
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    event.injectEventDoc(new XWikiDocument(eventDocRef));
    event.injectDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 15:30:00"));
    String displayPart = event.displayField("date-date_end.");
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>", displayPart);
    verifyDefault();
  }

  @Test
  public void testDisplayField_two_Dates_differentDays() throws ParseException {
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    event.injectEventDoc(new XWikiDocument(eventDocRef));
    event.injectDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    String displayPart = event.displayField("date-date_end");
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>"
        + "<span class=\"cel_cal_date_end\"> - 28.04.2009</span>", displayPart);
    verifyDefault();
  }

  @Test
  public void testDisplayField_startDate_only() throws ParseException {
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    event.injectEventDoc(new XWikiDocument(eventDocRef));
    event.injectDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", null);
    String displayPart = event.displayField("date-date_end");
    assertEquals("<span class=\"cel_cal_date\">27.04.2009</span>", displayPart);
    verifyDefault();
  }

  @Test
  public void testInternalDisplayField_two_Dates_differentDays (
      ) throws ParseException {
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    event.injectEventDoc(new XWikiDocument(eventDocRef));
    event.injectDefaultLanguage("de");
    BaseObject eventOjb = event.getObj("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    String displayPart = event.internalDisplayField("date-date_end", false);
    assertEquals("27.04.2009 - 28.04.2009", displayPart);
    verifyDefault();
  }

  @Test
  public void testGetNonEmptyFields_CombinedFields () throws ParseException {
    event.injectDefaultLanguage("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    List<String> resultList = event.getNonEmptyFields(fieldList);
    assertEquals("getNonEmptyFields must support combined fields",
        fieldList, resultList);
    verifyDefault();
  }

  @Test
  public void testGetNonEmptyFields_optionalDateTime (
      ) throws ParseException {
    event.injectEventDoc(new XWikiDocument(eventDocRef));
    event.injectDefaultLanguage("de");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("time.-time_end.");
    fieldList.add("date_end.");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 00:00:00"));
    List<String> resultList = event.getNonEmptyFields(fieldList);
    assertEquals("getNonEmptyFields must support optional time fields",
        Collections.emptyList(), resultList);
    verifyDefault();
  }

  @Test
  public void testNeedsMoreLink_multiple_used_fields (
      ) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    eventOjb.setLargeStringValue("contact_rte", "Gerbergasse 30");
    boolean needsMoreLink = event.needsMoreLink();
    verifyDefault();
    assertFalse("needsMoreLink must correctly handle multiple occurences.",
        needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_combinedFields (
      ) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    List<String> fieldList = new ArrayList<String>();
    fieldList.add("date-date_end");
    fieldList.add("time-time_end");
    fieldList.add("contact_rte-l_title");
    expect(calendar.getDetailviewFields()).andStubReturn(fieldList);
    List<String> emptylist = Collections.emptyList();
    expect(calendar.getOverviewFields()).andStubReturn(emptylist);
    //All additional fields in detailview are combined fields. If not correct
    //handled all fields will be emtpy and needsMoreLink returns wrongly false.
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    eventOjb.setLargeStringValue("contact_rte", "Gerbergasse 30");
    boolean needsMoreLink = event.needsMoreLink();
    verifyDefault();
    assertTrue("needsMoreLink must support compbined fields.", needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_identical_detailAndOvervFields_with_combinedFields (
      ) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 15:30:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    boolean needsMoreLink = event.needsMoreLink();
    verifyDefault();
    assertFalse("needsMoreLink must look at displayed property names.",
        needsMoreLink);
  }
  
  @Test
  public void testNeedsMoreLink_identical_optinalTimeEnd_in_detail (
      ) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 12:30:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    boolean needsMoreLink = event.needsMoreLink();
    verifyDefault();
    assertFalse("needsMoreLink must handle empty optional time_end field.",
        needsMoreLink);
  }
  
  @Test
  public void testGetDisplayPart_Empty_time_And_timeEnd() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setDateValue("eventDate_end", sdf.parse("28/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    String displayTimePart = event.getDisplayPart("time", true);
    String displayTimeEndPart = event.getDisplayPart("time_end", true);
    verifyDefault();
    assertEquals("Expecting empty String for optional time (00:00)",
        "", displayTimePart);
    assertEquals("Expecting empty String for optional time_end (00:00)",
        "", displayTimeEndPart);
  }

  @Test
  public void testInternalDisplayField_optional_time() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayDefault();
    BaseObject eventOjb = event.getObj("de");
    eventOjb.setDateValue("eventDate", sdf.parse("27/04/2009 00:00:00"));
    eventOjb.setStringValue("l_title", "Test Titel");
    eventOjb.setLargeStringValue("l_description", "Test Beschreibung");
    eventOjb.setLargeStringValue("location_rte", "Basel");
    String displayTimePart = event.internalDisplayField("time.", false);
    String displayTimeEndPart = event.internalDisplayField("time_end.", false);
    verifyDefault();
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
    replayDefault();
    assertEquals("time_end.", event.getDetailConfigForField("time_end"));
    assertEquals("time.", event.getDetailConfigForField("time"));
    verifyDefault();
  }
  
  @Test
  public void testIsOptionalField() {
    assertTrue(event.isIncludingFieldAsOptional("time_end", "time.-time_end."));
    assertTrue(event.isIncludingFieldAsOptional("time", "time.-time_end."));
    assertFalse(event.isIncludingFieldAsOptional("time_end", "time-time_end"));
    assertFalse(event.isIncludingFieldAsOptional("time", "time-time_end"));
    assertFalse(event.isIncludingFieldAsOptional("time", "date-date_end"));
  }

  @Test
  public void testEquals() throws Exception {
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    DocumentReference eventDoc2Ref = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    replayDefault();
    Event theEvent = new Event(eventDocRef);
    assertTrue(theEvent.equals(new Event(eventDoc2Ref)));
    verifyDefault();
  }

  @Test
  public void testEquals_listIndexOf() throws Exception {
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    DocumentReference eventDoc2Ref = new DocumentReference(context.getDatabase(),
        "myEventSpace", "Event1");
    replayDefault();
    Event theEvent = new Event(eventDocRef);
    assertEquals(0, Arrays.asList(theEvent).indexOf(new Event(eventDoc2Ref)));
    verifyDefault();
  }
  
  @Test
  public void testGetCalendar_set() throws Exception { 
    replayDefault();
    ICalendar ret = event.getCalendar();
    verifyDefault();
    
    assertSame(calendar, ret);
  }
  
  @Test
  public void testGetCalendar() throws Exception {
    event.injectCalendar(null);
    ICalendarService calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    event.injectCalService(calServiceMock);
    DocumentReference calDocRef = new DocumentReference("xwikidb", "someSpace", "calDoc");
    expect(calServiceMock.getCalendarDocRefByCalendarSpace("Test", new WikiReference(
        "xwikidb"))).andReturn(calDocRef).once();
    ICalendar cal = new Calendar(calDocRef, false);
    expect(calServiceMock.getCalendar(eq(calDocRef))).andReturn(cal).once();
 
    replayDefault();
    ICalendar ret = event.getCalendar();
    verifyDefault();
    
    assertSame(cal, ret);
  }
  
  @Test
  public void testGetCalendar_noCal() throws Exception {
    event.injectCalendar(null);
    ICalendarService calServiceMock = createMockAndAddToDefault(ICalendarService.class);
    event.injectCalService(calServiceMock);
    expect(calServiceMock.getCalendarDocRefByCalendarSpace("Test", new WikiReference(
        "xwikidb"))).andReturn(null).once();
 
    replayDefault();
    ICalendar ret = event.getCalendar();
    verifyDefault();
    
    assertNull(ret);
  }

}
