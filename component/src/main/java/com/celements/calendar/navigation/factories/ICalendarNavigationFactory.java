package com.celements.calendar.navigation.factories;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.search.SearchTermEventSearchQuery;

public interface ICalendarNavigationFactory {

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb);

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb, SearchTermEventSearchQuery query);

}
