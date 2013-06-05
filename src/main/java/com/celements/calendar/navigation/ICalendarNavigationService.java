package com.celements.calendar.navigation;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.search.EventSearchQuery;

@ComponentRole
public interface ICalendarNavigationService {

  public NavigationDetails getNavigationDetails(Date startDate, int offset);

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event);

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, EventSearchQuery query);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, EventSearchQuery query);

}
