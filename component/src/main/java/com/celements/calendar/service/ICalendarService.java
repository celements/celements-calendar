package com.celements.calendar.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.CalendarCreateException;
import com.celements.calendar.DateUtil;
import com.celements.calendar.ICalendar;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ICalendarService {

  @NotNull
  ICalendar createCalendar(@NotNull DocumentReference calConfigDocRef)
      throws CalendarCreateException;

  @NotNull
  ICalendar createCalendarArchive(@NotNull DocumentReference calConfigDocRef)
      throws CalendarCreateException;

  /**
   * @deprecated instead use {@link #createCalendar(DocumentReference)}
   */
  @Deprecated
  public ICalendar getCalendar(DocumentReference calDocRef);

  /**
   * @deprecated instead use {@link #createCalendar(DocumentReference)} and
   *             {@link ICalendar#setStartDate(Date)}
   */
  @Deprecated
  public ICalendar getCalendar(DocumentReference calDocRef, Date startDate);

  /**
   * @deprecated instead use {@link #createCalendarArchive(DocumentReference)}
   */
  @Deprecated
  public ICalendar getCalendarArchive(DocumentReference calDocRef);

  /**
   * @deprecated instead use {@link #createCalendarArchive(DocumentReference)} and
   *             {@link ICalendar#setStartDate(Date)}
   */
  @Deprecated
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
   * @deprecated instead use {@link #getCalendar(DocumentReference)} and
   *             {@link ICalendar#getEventSpaceRef()}
   */
  @Deprecated
  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef)
      throws XWikiException;

  /**
   * @deprecated instead use {@link #getCalendar(DocumentReference)} and
   *             {@link ICalendar#getEventSpaceRef()}
   */
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef) throws XWikiException;

  /**
   * @deprecated instead use {@link #getCalendar(DocumentReference)} and
   *             {@link ICalendar#getAllowedSpaceRefs()}
   */
  @Deprecated
  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException;

  /**
   * @deprecated instead use {@link #getCalendar(DocumentReference)} and
   *             {@link ICalendar#getAllowedSpaceRefs()}
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

  /**
   * @deprecated instead use {@link DateUtil#noTime(Date)}
   */
  @Deprecated
  public boolean isMidnightDate(Date date);

  /**
   * @deprecated instead use {@link DateUtil#noTime(Date)}
   */
  @Deprecated
  public Date getMidnightDate(Date date);

  /**
   * @deprecated instead use {@link DateUtil#endOfDay(Date)}
   */
  @Deprecated
  public Date getEndOfDayDate(Date date);

}
