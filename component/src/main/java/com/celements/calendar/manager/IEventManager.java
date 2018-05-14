package com.celements.calendar.manager;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IEventManager {

  public boolean addEvent(@NotNull ICalendar calendar, @NotNull DocumentReference eventDocRef)
      throws DocumentNotExistsException, DocumentSaveException;

  public boolean removeEvent(@NotNull ICalendar calendar, @NotNull DocumentReference eventDocRef)
      throws DocumentNotExistsException, DocumentSaveException;

  /**
   * @param calDoc
   * @param isArchive
   * @return
   * @deprecated instead use getEventsInternal(ICalendar, int, int)
   */
  @Deprecated
  public List<EventApi> getEvents(ICalendar cal, int start, int nb);

  public List<IEvent> getAllEventsInternal(ICalendar cal);

  public List<IEvent> getEventsInternal(ICalendar cal, int start, int nb);

  public EventSearchResult searchEvents(ICalendar cal, IEventSearchQuery query);

  /**
   * @param calDoc
   * @param isArchive
   * @return
   * @deprecated instead use countEvents(ICalendar)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive);

  /**
   * @param calDoc
   * @param isArchive
   * @param startDate
   * @return
   * @deprecated instead use countEvents(ICalendar)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate);

  /**
   * @param calDocRef
   * @param isArchive
   * @return
   * @deprecated instead use countEvents(ICalendar)
   */
  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive);

  /**
   * @param calDocRef
   * @param isArchive
   * @param startDate
   * @deprecated instead use countEvents(ICalendar)
   * @return
   */
  @Deprecated
  public long countEvents(DocumentReference calDocRef, boolean isArchive, Date startDate);

  public long countEvents(ICalendar cal);

  public IEvent getEvent(DocumentReference eventDocRef);

  public IEvent getFirstEvent(ICalendar cal);

  public IEvent getLastEvent(ICalendar cal);

}
