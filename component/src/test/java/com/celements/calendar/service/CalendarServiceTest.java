package com.celements.calendar.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
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
    xwiki = createMockAndAddToDefault(XWiki.class);
    context.setWiki(xwiki);
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    calService.injectQueryManager(queryManagerMock);
    queryMock = createMockAndAddToDefault(Query.class);
  }
  
  @Test
  public void testGetAllCalendars() throws QueryException {
    List<Object> fullNames = Arrays.asList(new Object[] {"space.asdf", "space.asdf2"});
    String xwql = getXWQL();
    
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(queryMock
        ).once();
    expect(queryMock.setWiki("xwikidb")).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayDefault();
    List<DocumentReference> allCals = calService.getAllCalendars();
    assertNotNull(allCals);
    assertEquals(2, allCals.size());
    assertEquals(new DocumentReference("xwikidb", "space", "asdf"), allCals.get(0));
    assertEquals(new DocumentReference("xwikidb", "space", "asdf2"), allCals.get(1));
    verifyDefault();
  }
  
  @Test
  public void testGetAllCalendars_Exception() throws QueryException {
    Throwable cause = new QueryException("", null, null);
    String xwql = getXWQL();
    
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andThrow(cause).once();
    
    replayDefault();
    List<DocumentReference> allCals = calService.getAllCalendars();
    assertNotNull(allCals);
    assertEquals(0, allCals.size());
    verifyDefault();
  }
  
  @Test
  public void testGetAllCalendars_exclude() throws QueryException {
    List<Object> fullNames = Arrays.asList(new Object[] {"space.asdf", "space.asdf2", 
        "space.asdf3"});
    List<DocumentReference> excludes = Arrays.asList(new DocumentReference("xwikidb", 
        "space", "asdf2"));
    String xwql = getXWQL();
    
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(queryMock
        ).once();
    expect(queryMock.setWiki("xwikidb")).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(fullNames).once();
    
    replayDefault();
    List<DocumentReference> allCals = calService.getAllCalendars(excludes);
    assertNotNull(allCals);
    assertEquals(2, allCals.size());
    assertEquals(new DocumentReference("xwikidb", "space", "asdf"), allCals.get(0));
    assertEquals(new DocumentReference("xwikidb", "space", "asdf3"), allCals.get(1));
    verifyDefault();
  }

  @Test
  public void testGetEventSpaceForCalendar_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

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

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

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

    replayDefault();
    String space = calService.getEventSpaceForCalendar(calDocRef);
    verifyDefault();

    assertNotNull(space);
    assertEquals("myCalSpace", space);
  }


  @Test
  public void testGetAllowedSpaces_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);

    expect(xwiki.getDocument(same(calDocRef), same(context))).andReturn(calDoc);

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

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

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

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

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

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

    replayDefault();
    List<String> spaces = calService.getAllowedSpaces(calDocRef);
    verifyDefault();

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

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

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

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

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

    replayDefault();
    String spacesHQL = calService.getAllowedSpacesHQL(calDoc);
    verifyDefault();

    assertNotNull(spacesHQL);
    assertEquals("(obj.name like 'myCalSpace.%' or obj.name like 'myCalSpace2.%'"
        + " or obj.name like 'myCalSpace3.%')", spacesHQL);
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_wikiRef() throws Exception {
    String database = "db";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", 
        "doc1"), new DocumentReference(database, "notMyInSpace", "doc"), 
        new DocumentReference(database, "myInSpace", "doc2")));
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace, 
        new WikiReference(database));
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_spaceRef() throws Exception {
    String database = "db";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", 
        "doc1"), new DocumentReference(database, "notMyInSpace", "doc"), 
        new DocumentReference(database, "myInSpace", "doc2")));
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace, 
        new SpaceReference("myInSpace", new WikiReference(database)));
    assertEquals(2, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(1));
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_nullRef() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", 
        "doc1"), new DocumentReference(database, "notMyInSpace", "doc"), 
        new DocumentReference(database, "myInSpace", "doc2")));
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace, 
        (SpaceReference) null);
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_noInSpace() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, "myInSpace", 
        "doc1"), new DocumentReference(database, "notMyInSpace", "doc"), 
        new DocumentReference(database, "myInSpace", "doc2")));
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    assertEquals(3, ret.size());
    assertEquals(calSpaceCache.get(calSpace).get(0), ret.get(0));
    assertEquals(calSpaceCache.get(calSpace).get(1), ret.get(1));
    assertEquals(calSpaceCache.get(calSpace).get(2), ret.get(2));
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefsByCalendarSpace_empty() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    List<DocumentReference> ret = calService.getCalendarDocRefsByCalendarSpace(calSpace);
    assertEquals(0, ret.size());
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefByCalendarSpace() throws Exception {    
    String database = "xwikidb";
    String inSpace = "myInSpace";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calSpaceCache.put(calSpace, Arrays.asList(new DocumentReference(database, 
        "notMyInSpace", "doc"), new DocumentReference(database, inSpace, "doc1"), 
        new DocumentReference(database, inSpace, "doc2")));
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace, inSpace);
    assertEquals(calSpaceCache.get(calSpace).get(1), ret);
    verifyDefault();
  }
  
  @Test
  public void testGetCalendarDocRefByCalendarSpace_empty() throws Exception {
    String database = "xwikidb";
    String calSpace = "myCalSpace";
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calService.injectCalCacheMap(database, calSpaceCache);
    
    replayDefault();
    DocumentReference ret = calService.getCalendarDocRefByCalendarSpace(calSpace);
    assertNull(ret);
    verifyDefault();
  }
  
  @Test
  public void testGetCalSpaceCache_inject() throws Exception {
    //TODO
  }
  
  @Test
  public void testGetCalSpaceCache_inject_empty() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    Map<String, List<DocumentReference>> calSpaceCache = 
        new HashMap<String, List<DocumentReference>>();
    calService.injectCalCacheMap("db", calSpaceCache);
    
    replayDefault();
    assertSame(calSpaceCache, calService.getCalSpaceCache(wikiRef));
    verifyDefault();
  }
  
  @Test
  public void testFilterForSpaceRef_null() throws Exception {
    List<DocumentReference> calDocRefs = new ArrayList<DocumentReference>();
    calDocRefs.add(new DocumentReference("db", "space", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "otherSpace", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "space", "calDoc2"));
    
    List<DocumentReference> ret = calService.filterForSpaceRef(calDocRefs, null);
    assertNotSame(calDocRefs, ret);
    assertEquals(calDocRefs, ret);
  }
  
  @Test
  public void testFilterForSpaceRef() throws Exception {
    List<DocumentReference> calDocRefs = new ArrayList<DocumentReference>();
    calDocRefs.add(new DocumentReference("db", "space", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "otherSpace", "calDoc"));
    calDocRefs.add(new DocumentReference("db", "space", "calDoc2"));
    SpaceReference inSpaceRef = new SpaceReference("space", new WikiReference("db"));
    
    List<DocumentReference> ret = calService.filterForSpaceRef(calDocRefs, inSpaceRef);
    assertNotSame(calDocRefs, ret);
    assertEquals(3, calDocRefs.size());
    assertEquals(2, ret.size());
    assertEquals(calDocRefs.get(0), ret.get(0));
    assertEquals(calDocRefs.get(2), ret.get(1));
  }
  
  private String getXWQL() {
    String xwql = "from doc.object(Classes.CalendarConfigClass) as cal where "
        + "doc.translation = 0";
    return xwql;
  }
  
}
