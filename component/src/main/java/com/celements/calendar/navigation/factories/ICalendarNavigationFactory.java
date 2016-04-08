package com.celements.calendar.navigation.factories;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.search.DateEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;

public interface ICalendarNavigationFactory {

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query
      ) throws LuceneSearchException;
  
  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query,
      boolean isSendingEmptyPage) throws LuceneSearchException;

}
