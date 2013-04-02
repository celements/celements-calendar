package com.celements.calendar.manager;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.Calendar;
import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.api.EventApi;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IEventManager {

  public List<EventApi> getEvents(ICalendar cal, int start, int nb);

  public List<IEvent> getEventsInternal(ICalendar cal, int start, int nb);

  /**
   * 
   * @param calDoc
   * @param isArchive
   * @return
   * 
   * @deprecated instead use countEvents(DocumentReference, boolean)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive);

  /**
   * 
   * @param calDoc
   * @param isArchive
   * @param startDate
   * @return
   * 
   * @deprecated instead use countEvents(DocumentReference, boolean, Date)
   */
  @Deprecated
  public long countEvents(XWikiDocument calDoc, boolean isArchive, Date startDate);
  
  public long countEvents(ICalendar cal);

  public long countEvents(DocumentReference calDocRef, boolean isArchive);

  public long countEvents(DocumentReference calDocRef, boolean isArchive, Date startDate);

  public NavigationDetails getNavigationDetails(Event event, Calendar cal
    ) throws XWikiException;

  public IEvent getEvent(DocumentReference eventDocRef);

}
