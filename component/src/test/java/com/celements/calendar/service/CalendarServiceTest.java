package com.celements.calendar.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CalendarServiceTest extends AbstractBridgedComponentTestCase {

  private CalendarService calService;
  private XWikiContext context;
  private XWiki xwiki;
  
  private QueryManager queryManagerMock;
  private Query queryMock;

  @Before
  public void setUp_CalendarServiceTest() throws Exception {
    context = getContext();
    calService = (CalendarService) Utils.getComponent(ICalendarService.class);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    queryManagerMock = createMock(QueryManager.class);
    calService.injectQueryManager(queryManagerMock);
    queryMock = createMock(Query.class);
  }
  
  @Test
  public void testGetAllCalendars() throws QueryException {
    List<Object> fullNames = Arrays.asList(new Object[] {"space.asdf", "space.asdf2"});
    String xwql = "from doc.object(Classes.CalendarConfigClass) as cal where "
        + "doc.translation = 0";
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(queryMock
        ).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayAll();
    List<DocumentReference> allCals = calService.getAllCalendars();
    assertNotNull(allCals);
    assertEquals(2, allCals.size());
    assertEquals(new DocumentReference("xwikidb", "space", "asdf"), allCals.get(0));
    assertEquals(new DocumentReference("xwikidb", "space", "asdf2"), allCals.get(1));
    verifyAll();
  }
  
  @Test
  public void testGetAllCalendars_Exception() throws QueryException {
    Throwable cause = new QueryException("", null, null);
    String xwql = "from doc.object(Classes.CalendarConfigClass) as cal where "
        + "doc.translation = 0";
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andThrow(cause).once();
    
    replayAll();
    List<DocumentReference> allCals = calService.getAllCalendars();
    assertNotNull(allCals);
    assertEquals(0, allCals.size());
    verifyAll();
  }
  
  @Test
  public void testGetAllCalendars_exclude() throws QueryException {
    List<Object> fullNames = Arrays.asList(new Object[] {"space.asdf", "space.asdf2", 
        "space.asdf3"});
    List<DocumentReference> excludes = Arrays.asList(new DocumentReference("xwikidb", 
        "space", "asdf2"));
    String xwql = "from doc.object(Classes.CalendarConfigClass) as cal where "
        + "doc.translation = 0";
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(queryMock
        ).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayAll();
    List<DocumentReference> allCals = calService.getAllCalendars(excludes);
    assertNotNull(allCals);
    assertEquals(2, allCals.size());
    assertEquals(new DocumentReference("xwikidb", "space", "asdf"), allCals.get(0));
    assertEquals(new DocumentReference("xwikidb", "space", "asdf3"), allCals.get(1));
    verifyAll();
  }

  @Test
  public void testGetEventSpaceForCalendar_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyAll();

    assertNotNull(space);
    assertEquals("myCalDoc", space);
  }

  @Test
  public void testGetEventSpaceForCalendar_emptyObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyAll();

    assertNotNull(space);
    assertEquals("", space);
  }

  @Test
  public void testGetEventSpaceForCalendar() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyAll();

    assertNotNull(space);
    assertEquals("myCalSpace", space);
  }


  @Test
  public void testGetAllowedSpaces_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyAll();

    assertNotNull(spaces);
    assertEquals(0, spaces.size());
  }

  @Test
  public void testGetAllowedSpaces_emptyObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyAll();

    assertNotNull(spaces);
    assertEquals(0, spaces.size());
  }

  @Test
  public void testGetAllowedSpaces() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyAll();

    assertNotNull(spaces);
    assertEquals(1, spaces.size());
    assertEquals("myCalSpace", spaces.get(0));
  }

  @Test
  public void testGetAllowedSpaces_subscribers() throws XWikiException {
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");

    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");
    List<String> subscribers = Arrays.asList("mySpace.myCalDoc2", "mySpace.myCalDoc3");
    calConfObj.setListValue("subscribe_to", subscribers);

    DocumentReference calDocRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc2");
    XWikiDocument calDoc2 = new XWikiDocument(calDocRef2);
    BaseObject calConfObj2 = new BaseObject();
    calConfObj2.setXClassReference(configClassRef);
    calDoc2.setXObject(0, calConfObj2);
    calConfObj2.setStringValue("calendarspace", "myCalSpace2");

    DocumentReference calDocRef3 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc3");
    XWikiDocument calDoc3 = new XWikiDocument(calDocRef3);
    BaseObject calConfObj3 = new BaseObject();
    calConfObj3.setXClassReference(configClassRef);
    calDoc3.setXObject(0, calConfObj3);
    calConfObj3.setStringValue("calendarspace", "myCalSpace3");

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(context))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(context))).andReturn(calDoc3).once();

    replayAll();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyAll();

    assertNotNull(spaces);
    assertEquals(3, spaces.size());
    assertEquals("myCalSpace", spaces.get(0));
    assertEquals("myCalSpace2", spaces.get(1));
    assertEquals("myCalSpace3", spaces.get(2));
  }

  @Test
  public void testGetAllowedSpacesHQL_none() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content",
        "Agenda");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyAll();

    assertNotNull(spacesHQL);
    assertEquals("(obj.name like '.%')", spacesHQL);
  }

  @Test
  public void testGetAllowedSpacesHQL() throws Exception {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayAll();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyAll();

    assertNotNull(spacesHQL);
    assertEquals("(obj.name like 'myCalSpace.%')", spacesHQL);
  }

  @Test
  public void testGetAllowedSpacesHQL_subscribers() throws XWikiException {
    DocumentReference configClassRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "CalendarConfigClass");

    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    BaseObject calConfObj = new BaseObject();
    calConfObj.setXClassReference(configClassRef);
    calDoc.setXObject(0, calConfObj);
    calConfObj.setStringValue("calendarspace", "myCalSpace");
    List<String> subscribers = Arrays.asList("mySpace.myCalDoc2", "mySpace.myCalDoc3");
    calConfObj.setListValue("subscribe_to", subscribers);

    DocumentReference calDocRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc2");
    XWikiDocument calDoc2 = new XWikiDocument(calDocRef2);
    BaseObject calConfObj2 = new BaseObject();
    calConfObj2.setXClassReference(configClassRef);
    calDoc2.setXObject(0, calConfObj2);
    calConfObj2.setStringValue("calendarspace", "myCalSpace2");

    DocumentReference calDocRef3 = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc3");
    XWikiDocument calDoc3 = new XWikiDocument(calDocRef3);
    BaseObject calConfObj3 = new BaseObject();
    calConfObj3.setXClassReference(configClassRef);
    calDoc3.setXObject(0, calConfObj3);
    calConfObj3.setStringValue("calendarspace", "myCalSpace3");

    expect(xwiki.getDocument(eq(calDocRef), same(context))).andReturn(calDoc).once();
    expect(xwiki.getDocument(eq(calDocRef2), same(context))).andReturn(calDoc2).once();
    expect(xwiki.getDocument(eq(calDocRef3), same(context))).andReturn(calDoc3).once();

    replayAll();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyAll();

    assertNotNull(spacesHQL);
    assertEquals("(obj.name like 'myCalSpace.%' or obj.name like 'myCalSpace2.%'"
        + " or obj.name like 'myCalSpace3.%')", spacesHQL);
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace() throws Exception {
    String calSpace = "myCalSpace";
    String inSpace = "myInSpace";
    List<Object> fullNames = Arrays.asList((Object) "myInSpace.doc1", 
        (Object) "myInSpace.doc2");
    
    expect(queryManagerMock.createQuery(eq(getXWQL(true)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.bindValue(eq("docSpace"), eq(inSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayAll();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace, 
        inSpace);
    assertEquals(2, ret.size());
    assertEquals(new DocumentReference("xwikidb", "myInSpace", "doc1"), ret.get(0));
    assertEquals(new DocumentReference("xwikidb", "myInSpace", "doc2"), ret.get(1));
    verifyAll();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_noInSpace() throws Exception {
    String calSpace = "myCalSpace";
    List<Object> fullNames = Arrays.asList((Object) "myInSpace.doc1", 
        (Object) "myInSpace.doc2");
    
    expect(queryManagerMock.createQuery(eq(getXWQL(false)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayAll();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    assertEquals(2, ret.size());
    assertEquals(new DocumentReference("xwikidb", "myInSpace", "doc1"), ret.get(0));
    assertEquals(new DocumentReference("xwikidb", "myInSpace", "doc2"), ret.get(1));
    verifyAll();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_empty() throws Exception {
    String calSpace = "myCalSpace";
    
    expect(queryManagerMock.createQuery(eq(getXWQL(false)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(Collections.emptyList()).once();
    
    replayAll();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    assertEquals(0, ret.size());
    verifyAll();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_error() throws Exception {
    String calSpace = "myCalSpace";
    
    expect(queryManagerMock.createQuery(eq(getXWQL(false)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andThrow(new QueryException("", null, null)).once();
    
    replayAll();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    assertEquals(0, ret.size());
    verifyAll();
  }
  
  @Test
  public void testGetCalendarDocRefByCalendarSpace() throws Exception {
    String calSpace = "myCalSpace";
    String inSpace = "myInSpace";
    List<Object> fullNames = Arrays.asList((Object) "myInSpace.doc1", 
        (Object) "myInSpace.doc2");
    
    expect(queryManagerMock.createQuery(eq(getXWQL(true)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.bindValue(eq("docSpace"), eq(inSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayAll();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace, inSpace);
    assertEquals(new DocumentReference("xwikidb", "myInSpace", "doc1"), ret);
    verifyAll();
  }
  
  @Test
  public void testGetCalendarDocRefByCalendarSpace_empty() throws Exception {
    String calSpace = "myCalSpace";
    String inSpace = "myInSpace";
    
    expect(queryManagerMock.createQuery(eq(getXWQL(true)), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("calSpace"), eq(calSpace))).andReturn(queryMock).once();
    expect(queryMock.bindValue(eq("docSpace"), eq(inSpace))).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(Collections.emptyList()).once();
    
    replayAll();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace, inSpace);
    assertNull(ret);
    verifyAll();
  }
  
  @Test
  public void testGetCalendarsForCalSpaceXWQL() {
    assertEquals(getXWQL(true), calService.getCalendarsForCalSpaceXWQL(true));
    assertEquals(getXWQL(false), calService.getCalendarsForCalSpaceXWQL(false));
  }
  
  private String getXWQL(boolean withDocSpace) {
    String xwql = "FROM doc.object(Classes.CalendarConfigClass) AS calConfig "
        + "WHERE calConfig.calendarspace = :calSpace";
    if (withDocSpace) {
      xwql += " AND doc.space = :docSpace";
    }
    return xwql;
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, queryManagerMock, queryMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, queryManagerMock, queryMock);
    verify(mocks);
  }
}
