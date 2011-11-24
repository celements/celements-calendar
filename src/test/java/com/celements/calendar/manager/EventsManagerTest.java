package com.celements.calendar.manager;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Event;
import com.celements.calendar.service.ICalendarService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class EventsManagerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private EventsManager eventsMgr;
  private XWiki xwiki;
  private XWikiStoreInterface mockStore;
  private Execution executionMock;
  private ICalendarService calServiceMock;

  @Before
  public void setUp_GetEventsCommandTest() throws Exception {
    context = getContext();
    eventsMgr = new EventsManager();
    executionMock = createMock(Execution.class);
    eventsMgr.execution = executionMock;
    ExecutionContext execContext = new ExecutionContext();
    execContext.setProperty("xwikicontext", context);
    expect(executionMock.getContext()).andReturn(execContext).anyTimes();
    calServiceMock = createMock(ICalendarService.class);
    eventsMgr.calService = calServiceMock;
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockStore).anyTimes();
  }

  @Test
  public void testIsHomeCalendar() throws Exception {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
    "myCalDoc");
    DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
        "Event1");
    XWikiDocument eventDoc = new XWikiDocument(eventDocRef);
    expect(xwiki.getDocument(eq(eventDocRef), same(context))).andReturn(eventDoc).once();
    expect(calServiceMock.getEventSpaceForCalendar(eq(calDocRef), same(context))
        ).andReturn("inbox").once();
    replayAll();
    Event theEvent = new Event(eventDocRef, context);
    assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
        + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, theEvent));
    verifyAll();
  }

  @Test
  public void testCountEvents() throws Exception {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    List<Object> resultList = Collections.emptyList();
    expect(mockStore.search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).once();
    replayAll();
    assertNotNull(eventsMgr.countEvents(calDoc, false));
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, mockStore, executionMock, calServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockStore, executionMock, calServiceMock);
    verify(mocks);
  }

}
