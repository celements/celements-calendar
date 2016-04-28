package com.celements.calendar;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CalendarClassConfigTest extends AbstractComponentTest {
  
  private ICalendarClassConfig calClassConf;
  
  @Before
  public void setUp_CalendarClassConfigTest() throws Exception {
    calClassConf = Utils.getComponent(ICalendarClassConfig.class);
  }

  @Test
  public void testGetCalendarClassRef() {
    replayDefault();
    DocumentReference calClassRef = calClassConf.getCalendarClassRef();
    verifyDefault();
    assertNotNull(calClassRef);
  }

}
