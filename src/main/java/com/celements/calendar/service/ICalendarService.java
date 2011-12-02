package com.celements.calendar.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ICalendarService {

  public String getEventSpaceForCalendar(DocumentReference calDocRef
      ) throws XWikiException;

  public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException;

  public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive);

}
