package com.celements.calendar.navigation.factories;

import java.util.Date;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.search.IEventSearchQuery;

public interface INavigationDetailsFactory {

  public NavigationDetails getNavigationDetails(Date startDate, int offset);

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event);

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, IEventSearchQuery query);

}
