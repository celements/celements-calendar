package com.celements.calendar.navigation;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface ICalendarNavigationService {

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event) throws XWikiException;

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      Date eventDate, int offset, int nb) throws XWikiException;

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb) throws XWikiException;

}
