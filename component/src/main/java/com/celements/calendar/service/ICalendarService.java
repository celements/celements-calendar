package com.celements.calendar.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ICalendarService {

  public ICalendar getCalendar(DocumentReference calDocRef);

  public ICalendar getCalendar(DocumentReference calDocRef, Date startDate);

  public ICalendar getCalendarArchive(DocumentReference calDocRef);

  public ICalendar getCalendarArchive(DocumentReference calDocRef, Date startDate);

  /**
   * @deprecated instead use {@link #getCalendar} or {@link #getCalendarArchive}
   */
  @Deprecated
  public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive);

  /**
   * gets all calendars in current database
   *
   * @return list of calendar config doc refs
   */
  public List<DocumentReference> getAllCalendars();

  /**
   * gets all calendars in given database
   *
   * @param wikiRef
   *          database
   * @return list of calendar config doc refs
   */
  public List<DocumentReference> getAllCalendars(WikiReference wikiRef);

  /**
   * gets all calendars in current database without excludes
   *
   * @param excludes
   *          calendar config doc refs which to exclude
   * @return list of calendar config doc refs
   */
  public List<DocumentReference> getAllCalendars(Collection<DocumentReference> excludes);

  /**
   * gets all calendars in given database without excludes
   *
   * @param wikiRef
   *          database
   * @param excludes
   *          calendar config doc refs which to exclude
   * @return list of calendar config doc refs
   */
  public List<DocumentReference> getAllCalendars(WikiReference wikiRef,
      Collection<DocumentReference> excludes);

  /**
   * @deprecated instead use {@link #getCalendarEventSpace}
   */
  @Deprecated
  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef)
      throws XWikiException;

  /**
   * @deprecated instead use {@link #getCalendarEventSpace}
   */
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef) throws XWikiException;

  public SpaceReference getCalendarEventSpace(DocumentReference calDocRef)
      throws DocumentNotExistsException;

  public List<SpaceReference> getAllowedCalendarSpaces(DocumentReference calDocRef)
      throws DocumentNotExistsException;

  /**
   * @deprecated instead use {@link #getAllowedCalendarSpaces}
   */
  @Deprecated
  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException;

  /**
   * @deprecated instead use {@link #getAllowedCalendarSpaces}
   */
  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException;

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace);

  /**
   * @deprecated instead use {@link #getCalendarDocRefByCalendarSpace(String,
   *             EntityReference)}
   */
  @Deprecated
  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace, String inSpace);

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace, EntityReference inRef);

  /**
   * @param calSpace
   *          space name which to get calendars for
   * @return all calendar config docs with given space name
   */
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace);

  /**
   * @deprecated instead use {@link #getCalendarDocRefsByCalendarSpace(String,
   *             EntityReference)}
   */
  @Deprecated
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace, String inSpace);

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

  public boolean isMidnightDate(Date date);

  public Date getMidnightDate(Date date);

  public Date getEndOfDayDate(Date date);

}
