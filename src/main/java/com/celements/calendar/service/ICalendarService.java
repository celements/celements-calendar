package com.celements.calendar.service;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface ICalendarService {

  public String getEventSpaceForCalendar(DocumentReference calDocRef, XWikiContext context
      ) throws XWikiException;

  public void setStartDate(Date newStartDate);

  public Date getStartDate();

}
