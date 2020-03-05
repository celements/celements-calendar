package com.celements.calendar.navigation.factories;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.search.IDateEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;

public interface ICalendarNavigationFactory {

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb);
  
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, boolean isSendingEmptyPage);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, IDateEventSearchQuery query
      ) throws LuceneSearchException;
  
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, IDateEventSearchQuery query,
      boolean isSendingEmptyPage) throws LuceneSearchException;

}
