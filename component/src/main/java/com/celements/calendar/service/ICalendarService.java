package com.celements.calendar.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

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

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace,
      String inSpace);

  /**
   * 
   * @param calSpace
   *          space name which to get calendars for
   * @return all calendar config docs with given space name
   */
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace);

  /**
   * @param calSpace
   *          space name which to get calendars for
   * @param inSpace
   *          only returns calendars within this space
   * @return all calendar config docs with given space name and within given space
   */
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace,
      String inSpace);

  /**
   * @param calSpace
   *          space name which to get calendars for
   * @param inRef
   *          only returns calendars within this reference - either {@link WikiReference}
   *          or {@link SpaceReference}
   * @return all calendar config docs with given space name and within given reference
   */
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace,
      EntityReference inRef);

  public Date getMidnightDate(Date date);

}
