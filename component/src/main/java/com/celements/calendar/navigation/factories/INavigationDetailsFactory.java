package com.celements.calendar.navigation.factories;

import java.util.Date;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;

public interface INavigationDetailsFactory {

  public NavigationDetails getNavigationDetails(Date startDate, int offset
      ) throws NavigationDetailException;

  public NavigationDetails getNavigationDetails(DocumentReference calDocRef, IEvent event
      ) throws NavigationDetailException;

  public NavigationDetails getNavigationDetails(DocumentReference calDocRef, IEvent event, 
      IEventSearchQuery query) throws NavigationDetailException, LuceneSearchException;

}
