package com.celements.calendar.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetEventsCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private GetEventsCommand getEventsCmd;
  private XWiki xwiki;
  private XWikiStoreInterface mockStore;

  @Before
  public void setUp_GetEventsCommandTest() throws Exception {
    context = getContext();
    getEventsCmd = new GetEventsCommand();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockStore).anyTimes();
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
    assertNotNull(getEventsCmd.countEvents(calDoc, false, context));
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, mockStore);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockStore);
    verify(mocks);
  }

}
