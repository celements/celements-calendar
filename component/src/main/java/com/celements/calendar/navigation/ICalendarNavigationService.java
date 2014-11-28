package com.celements.calendar.navigation;

import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.NavigationDetailException;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.search.DateEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;

@ComponentRole
public interface ICalendarNavigationService {

  public NavigationDetails getNavigationDetails(Date startDate, int offset
      ) throws NavigationDetailException;

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event) throws NavigationDetailException;

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, IEventSearchQuery query) throws NavigationDetailException, 
      LuceneSearchException;

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query
      ) throws LuceneSearchException;

}
