package com.celements.calendar.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ICalendarService {

  public List<DocumentReference> getAllCalendars();

  public List<DocumentReference> getAllCalendars(Collection<DocumentReference> excludes);

  public String getEventSpaceForCalendar(DocumentReference calDocRef
      ) throws XWikiException;

  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException;

  /**
   * 
   * @Deprecated use getAllowedSpaces(DocumentReference) instead
   */
  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException;

  public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive);

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace);

  public Date getMidnightDate(Date date);

}
