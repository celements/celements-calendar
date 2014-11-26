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
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ICalendarService {

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

  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef
      ) throws XWikiException;

  /**
   * @deprecated instead use {@link #getEventSpaceRefForCalendar}
   */
  @Deprecated
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
  
  public boolean isMidnightDate(Date date);

  public Date getMidnightDate(Date date);
  
  public Date getEndOfDayDate(Date date);

}
