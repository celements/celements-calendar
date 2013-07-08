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
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class CalendarEngineHQLTest extends AbstractBridgedComponentTestCase {

  private CalendarEngineHQL engine;
  private QueryManager queryManagerMock;

  @Before
  public void setUp_EventsManagerTest() {
    engine = (CalendarEngineHQL) Utils.getComponent(ICalendarEngineRole.class, "hql");
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    engine.injectQueryManager(queryManagerMock);
  }

  @Test
  public void testGetFirstEventDate() throws QueryException, Exception {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = false;
    String eventFullName = "TestSpace.TestEvent";
    List<Object> list = new ArrayList<Object>();
    list.add(eventFullName);

    Query queryMock = createMock(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setOffset(0)).andReturn(queryMock).once();
    expect(queryMock.setLimit(1)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(list).once();

    replayDefault(queryMock);
    IEvent firstEvent = engine.getFirstEvent(startDate, isArchive, lang, spaces);
    verifyDefault(queryMock);

    assertEquals(new Event(new DocumentReference("xwikidb", "TestSpace", "TestEvent")),
        firstEvent);
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
    String eventFullName = "TestSpace.TestEvent";
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(eventFullName);

    Query queryMock1 = createMock(Query.class);
    Query queryMock2 = createMock(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(true, isArchive)), eq("hql"))
        ).andReturn(queryMock1).once();
    expect(queryMock1.execute()).andReturn(countList).once();

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock2).once();
    expect(queryMock2.setOffset(eq((int) count - 1))).andReturn(queryMock2).once();
    expect(queryMock2.setLimit(eq(1))).andReturn(queryMock2).once();
    expect(queryMock2.execute()).andReturn(eventList).once();

    replayDefault(queryMock1, queryMock2);
    IEvent firstEvent = engine.getFirstEvent(startDate, isArchive, lang, spaces);
    verifyDefault(queryMock1, queryMock2);

    assertEquals(new Event(new DocumentReference("xwikidb", "TestSpace", "TestEvent")),
        firstEvent);
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
    String eventFullName = "TestSpace.TestEvent";
    List<Object> eventList = new ArrayList<Object>();
    eventList.add(eventFullName);

    Query queryMock1 = createMock(Query.class);
    Query queryMock2 = createMock(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(true, isArchive)), eq("hql"))
        ).andReturn(queryMock1).once();
    expect(queryMock1.execute()).andReturn(countList).once();

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock2).once();
    expect(queryMock2.setOffset(eq((int) count - 1))).andReturn(queryMock2).once();
    expect(queryMock2.setLimit(eq(1))).andReturn(queryMock2).once();
    expect(queryMock2.execute()).andReturn(eventList).once();

    replayDefault(queryMock1, queryMock2);
    IEvent lastEvent = engine.getLastEvent(startDate, isArchive, lang, spaces);
    verifyDefault(queryMock1, queryMock2);

    assertEquals(new Event(new DocumentReference("xwikidb", "TestSpace", "TestEvent")),
        lastEvent);
  }

  @Test
  public void testGetLastEventDate_isArchive() throws QueryException {
    String lang = "de";
    List<String> spaces = Arrays.asList("myCalSpace");
    Date startDate = new Date(0);
    boolean isArchive = true;
    String eventFullName = "TestSpace.TestEvent";
    List<Object> list = new ArrayList<Object>();
    list.add(eventFullName);

    Query queryMock = createMock(Query.class);

    expect(queryManagerMock.createQuery(eq(getHQL(false, isArchive)), eq("hql"))
        ).andReturn(queryMock).once();
    expect(queryMock.setOffset(0)).andReturn(queryMock).once();
    expect(queryMock.setLimit(1)).andReturn(queryMock).once();
    expect(queryMock.execute()).andReturn(list).once();

    replayDefault(queryMock);
    IEvent lastEvent = engine.getLastEvent(startDate, isArchive, lang, spaces);
    verifyDefault(queryMock);

    assertEquals(new Event(new DocumentReference("xwikidb", "TestSpace", "TestEvent")),
        lastEvent);
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

}
