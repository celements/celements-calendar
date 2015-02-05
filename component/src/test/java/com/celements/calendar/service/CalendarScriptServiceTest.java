package com.celements.calendar.service;

import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.calendar.search.IEventSearchQuery;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class CalendarScriptServiceTest extends AbstractBridgedComponentTestCase{

  private CalendarScriptService calScriptService;
  private XWikiContext context;
  private LuceneQuery luceneQueryAfter;

  @Before
  public void setUp_CalendarScriptServiceTest() throws Exception {
    context = getContext();
    calScriptService = (CalendarScriptService) Utils.getComponent(ScriptService.class,
        "celcalendar");
  }

  @Test
  public void testGetEventSearchQuery() {
    LuceneQuery luceneQuery = new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
    QueryRestriction restriction = new QueryRestriction("Classes.CalendarEventClass.org_id", 
        "\"4051\"");
    luceneQuery.add(restriction);
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
