package com.celements.calendar.engine;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.web.Utils;

public class CalendarEngineHQLTest extends AbstractBridgedComponentTestCase {

  private CalendarEngineHQL engine;
  private QueryManager queryManagerMock;
  private XWiki xwiki;
  private ICalendar calMock;
  private String database;

  @Before
  public void setUp_CalendarEngineHQLTest() {
    engine = (CalendarEngineHQL) Utils.getComponent(ICalendarEngineRole.class);
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    engine.injectQueryManager(queryManagerMock);
    xwiki = createMockAndAddToDefault(XWiki.class);
    getContext().setWiki(xwiki);
    expect(xwiki.getXWikiPreferenceAsInt(eq("calendar_toArchiveOnEndDate"), 
        eq("celements.calendar.toArchiveOnEndDate"), eq(1), same(getContext()))
        ).andReturn(0).anyTimes();
    calMock = createMockAndAddToDefault(ICalendar.class);
    database = "theDB";
    DocumentReference docRef = new DocumentReference(database, "someSpace", "someCal");
    expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(calMock.getWikiRef()).andReturn(new WikiReference(database)).anyTimes();
  }

  @Test
  public void testGetName() {
    assertEquals("hql", engine.getName());
  }

  @Test
  public void testGetEngineLimit() {
    assertEquals(0, engine.getEngineLimit());
  }

  @Test
  public void testCountEventsInternal() throws QueryException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = false;
    long count = 5L;
    Query queryMock = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(true, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setWiki(database)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(Arrays.<Object>asList(count)).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    long ret = engine.countEventsInternal(calMock);
    verifyDefault();

    assertEquals(count, ret);
  }

  @Test
  public void testGetEvents() throws QueryException, Exception {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = true;
    DocumentReference evDocRef1 = new DocumentReference(database, "space", "ev1");
    DocumentReference evDocRef2 = new DocumentReference(database, "space", "ev2");
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef1));
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef2));

    Query queryMock = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setWiki(database)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(eventList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    List<IEvent> ret = engine.getEvents(calMock, 0, 0);
    verifyDefault();

    assertEquals(2, ret.size());
    assertEquals(new Event(evDocRef1), ret.get(0));
    assertEquals(new Event(evDocRef2), ret.get(1));
  }

  @Test
  public void testGetEvents_limit_offset() throws QueryException, Exception {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = true;
    int offset = 10;
    int limit = 20;
    DocumentReference evDocRef1 = new DocumentReference(database, "space", "ev1");
    DocumentReference evDocRef2 = new DocumentReference(database, "space", "ev2");
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef1));
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef2));

    Query queryMock = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setWiki(database)).andReturn(queryMock).once();
    expect(queryMock.setOffset(offset)).andReturn(queryMock).once();
    expect(queryMock.setLimit(limit)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(eventList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    List<IEvent> ret = engine.getEvents(calMock, offset, limit);
    verifyDefault();

    assertEquals(2, ret.size());
    assertEquals(new Event(evDocRef1), ret.get(0));
    assertEquals(new Event(evDocRef2), ret.get(1));
  }

  @Test
  public void testGetFirstEventDate() throws QueryException, Exception {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = false;
    DocumentReference evDocRef = new DocumentReference(database, "space", "ev");
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef));

    Query queryMock = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setWiki(database)).andReturn(queryMock).once();
    expect(queryMock.setLimit(1)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(eventList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();

    assertEquals(new Event(evDocRef), ret);
  }

  @Test
  public void testGetFirstEventDate_isArchive() throws QueryException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = true;
    long count = 5L;
    List<Object> countList = new ArrayList<Object>();
    countList.add(count);
    DocumentReference evDocRef = new DocumentReference(database, "space", "ev");
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef));

    Query queryMock1 = createMockAndAddToDefault(Query.class);
    Query queryMock2 = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(true, isArchive)), eq("hql"))
        ).andReturn(queryMock1).once();
    expect(queryMock1.setWiki(database)).andReturn(queryMock1).once();
    expect(queryMock1.execute()).andReturn(countList).once();

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock2).once();
    expect(queryMock2.setWiki(database)).andReturn(queryMock2).once();
    expect(queryMock2.setOffset(eq((int) count - 1))).andReturn(queryMock2).once();
    expect(queryMock2.setLimit(eq(1))).andReturn(queryMock2).once();
    expect(queryMock2.execute()).andReturn(eventList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    IEvent ret = engine.getFirstEvent(calMock);
    verifyDefault();

    assertEquals(new Event(evDocRef), ret);
  }

  @Test
  public void testGetLastEventDate() throws QueryException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = false;
    long count = 5L;
    List<Object> countList = new ArrayList<Object>();
    countList.add(count);
    DocumentReference evDocRef = new DocumentReference(database, "space", "ev");
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef));

    Query queryMock1 = createMockAndAddToDefault(Query.class);
    Query queryMock2 = createMockAndAddToDefault(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(true, isArchive)), eq("hql"))
        ).andReturn(queryMock1).once();
    expect(queryMock1.setWiki(database)).andReturn(queryMock1).once();
    expect(queryMock1.execute()).andReturn(countList).once();

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock2).once();
    expect(queryMock2.setWiki(database)).andReturn(queryMock2).once();
    expect(queryMock2.setOffset(eq((int) count - 1))).andReturn(queryMock2).once();
    expect(queryMock2.setLimit(eq(1))).andReturn(queryMock2).once();
    expect(queryMock2.execute()).andReturn(eventList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();

    assertEquals(new Event(evDocRef), ret);
  }

  @Test
  public void testGetLastEventDate_isArchive() throws QueryException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = true;
    DocumentReference evDocRef = new DocumentReference(database, "space", "ev");
    List<Object> evList = new ArrayList<Object>();
    evList.add(getWebUtilsService().getRefLocalSerializer().serialize(evDocRef));

    Query queryMock = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setWiki(database)).andReturn(queryMock).once();
    expect(queryMock.setLimit(1)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(evList).once();
    expectForCalMock(startDate, isArchive, lang, spaces);

    replayDefault();
    IEvent ret = engine.getLastEvent(calMock);
    verifyDefault();

    assertEquals(new Event(evDocRef), ret);
  }

  private void expectForCalMock(Date startDate, boolean isArchive, String lang, 
      List<String> spaces) {
    expect(calMock.getStartDate()).andReturn(startDate).atLeastOnce();
    expect(calMock.isArchive()).andReturn(isArchive).atLeastOnce();
    expect(calMock.getLanguage()).andReturn(lang).atLeastOnce();
    expect(calMock.getAllowedSpaces()).andReturn(spaces).atLeastOnce();
  }

  private String getHQL(boolean isCount, boolean isArchive) {
    String select = isCount ? "count(obj.name)" : "obj.name";
    String comp = isArchive ? "<" : ">=";
    String order = isArchive ? "desc" : "asc";
    String selectEmptyDates = isArchive ? "" : "or ec.eventDate is null";
    return "select " + select + " from BaseObject as obj, Classes.CalendarEventClass as "
    +	"ec where ec.id.id=obj.id and obj.className = 'Classes.CalendarEventClass' and "
    + "ec.lang='de' and (ec.eventDate " + comp + " '1970-01-01 01:00:00' "
    + selectEmptyDates + ") and (obj.name like 'myCalSpace.%') order by ec.eventDate "
    + order + ", ec.eventDate_end " + order + ", ec.l_title " + order;
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
