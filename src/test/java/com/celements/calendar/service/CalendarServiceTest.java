package com.celements.calendar.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class CalendarServiceTest extends AbstractBridgedComponentTestCase {

  private CalendarService calService;
  private XWikiContext context;
  private Execution executionMock;
  private XWiki xwiki;
  private XWikiStoreInterface mockStore;

  @Before
  public void setUp_CalendarServiceTest() throws Exception {
    context = getContext();
    calService = new CalendarService();
    executionMock = createMock(Execution.class);
    calService.execution = executionMock;
    ExecutionContext execContext = new ExecutionContext();
    execContext.setProperty("xwikicontext", context);
    expect(executionMock.getContext()).andReturn(execContext).anyTimes();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockStore).anyTimes();
  }

  @Test
  public void testGetAllowedSpacesHQL() throws Exception {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myCalDoc");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    DocumentReference calConfObjRef = new DocumentReference(context.getDatabase(), 
        CelementsCalendarPlugin.CLASS_CALENDAR_SPACE, 
        CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
    BaseObject calConfigObj = new BaseObject();
    calConfigObj.setXClassReference(calConfObjRef);
    calDoc.setXObject(0, calConfigObj);
    calConfigObj.setStringValue(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE,
        "myCalSpace");
    List<String> subscrList = Arrays.asList("mySpace.cal1", "mySpace.cal2");
    calConfigObj.setListValue(CelementsCalendarPlugin.PROPERTY_SUBSCRIBE_TO, subscrList);
    DocumentReference cal1Ref = new DocumentReference(context.getDatabase(), "mySpace",
        "cal1");
    XWikiDocument cal1Doc = new XWikiDocument(cal1Ref);
    expect(xwiki.exists(eq(cal1Ref), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(cal1Ref), same(context))).andReturn(cal1Doc).once();
    BaseObject cal1ConfigObj = new BaseObject();
    cal1ConfigObj.setXClassReference(calConfObjRef);
    cal1Doc.setXObject(0, cal1ConfigObj);
    cal1ConfigObj.setStringValue(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE,
        "myCal1Space");
    DocumentReference cal2Ref = new DocumentReference(context.getDatabase(), "mySpace",
        "cal2");
    XWikiDocument cal2Doc = new XWikiDocument(cal2Ref);
    expect(xwiki.exists(eq(cal2Ref), same(context))).andReturn(true).once();
    expect(xwiki.getDocument(eq(cal2Ref), same(context))).andReturn(cal2Doc).once();
    BaseObject cal2ConfigObj = new BaseObject();
    cal2ConfigObj.setXClassReference(calConfObjRef);
    cal2Doc.setXObject(0, cal2ConfigObj);
    cal2ConfigObj.setStringValue(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE,
        "myCal2Space");
    replayAll();
    assertEquals("(obj.name like 'myCalSpace.%' or obj.name like 'myCal1Space.%'"
        + " or obj.name like 'myCal2Space.%')", calService.getAllowedSpacesHQL(calDoc));
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, mockStore, executionMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockStore, executionMock);
    verify(mocks);
  }
}
