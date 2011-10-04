/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.calendar.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class CalendarUtilsTest extends AbstractBridgedComponentTestCase {

  private ICalendarUtils calUtils;
  private XWikiContext context;

  @Before
  public void setUp_CalendarUtilsTest() throws Exception {
    calUtils = CalendarUtils.getInstance();
    context = getContext();
  }

  @Test
  public void testGetInstance() {
    assertNotNull("getInstance should always return an instance.", calUtils);
    assertSame("Should be singleton", calUtils, CalendarUtils.getInstance());
  }

  @Test
  public void testGetAllowedSpacesHQL_noObject() throws XWikiException {
    DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "Content",
        "Agenda");
    XWikiDocument calDoc = new XWikiDocument(calDocRef);
    assertEquals("obj.name like '.%'", calUtils.getAllowedSpacesHQL(calDoc , context));
  }

}
