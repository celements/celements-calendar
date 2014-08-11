package com.celements.calendar.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class CalendarScriptServiceTest extends AbstractBridgedComponentTestCase{

  private CalendarScriptService calScriptService;
  private XWikiContext context;
  private LuceneQueryApi luceneQueryAfter;

  @Before
  public void setUp_CalendarScriptServiceTest() throws Exception {
    context = getContext();
    calScriptService = (CalendarScriptService) Utils.getComponent(ScriptService.class,
        "celcalendar");
  }

  @Test
  public void testGetEventSearchQuery() {
    LuceneQueryApi luceneQuery = new LuceneQueryApi(context.getDatabase());
    LuceneQueryRestrictionApi restriction = new LuceneQueryRestrictionApi(
        "Classes.CalendarEventClass.org_id", "\"4051\"");
    luceneQuery.addRestriction(restriction);
    String expectedQueryString = luceneQuery.getQueryString();
    replayDefault();
    IEventSearchQuery eventSearchQuery = calScriptService.getEventSearchQuery(luceneQuery,
        null);
    luceneQueryAfter = eventSearchQuery.getAsLuceneQuery();
    assertEquals("lucene query may not be changed inside getEventSearchQuery",
        expectedQueryString, luceneQuery.getQueryString());
    assertNotSame(luceneQuery, luceneQueryAfter);
    verifyDefault();
  }

}