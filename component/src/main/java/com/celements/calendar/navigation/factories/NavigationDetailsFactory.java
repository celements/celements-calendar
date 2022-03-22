package com.celements.calendar.navigation.factories;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.calendar.service.ICalendarService;
import com.celements.search.lucene.LuceneSearchException;
import com.xpn.xwiki.web.Utils;

public class NavigationDetailsFactory implements INavigationDetailsFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      NavigationDetailsFactory.class);

  private ICalendarService calService;

  @Override
  public NavigationDetails getNavigationDetails(Date startDate, int offset)
      throws NavigationDetailException {
    return NavigationDetails.create(startDate, offset);
  }

  @Override
  public NavigationDetails getNavigationDetails(DocumentReference calDocRef, IEvent event)
      throws NavigationDetailException {
    try {
      return getNavigationDetails(calDocRef, event, null);
    } catch (LuceneSearchException lse) {
      // should never happen
      LOGGER.error("search failed for cal '{}', event '{}'", calDocRef, event, lse);
    }
    return null;
  }

  @Override
  public NavigationDetails getNavigationDetails(DocumentReference calDocRef, IEvent event,
      IEventSearchQuery query) throws NavigationDetailException, LuceneSearchException {
    NavigationDetails navDetail = null;
    LOGGER.debug("getNavigationDetails: for cal '{}', event '{}'", calDocRef, event);
    Date eventDate = event.getEventDate();
    if (eventDate != null) {
      ICalendar cal = getCalService().getCalendar(calDocRef, eventDate);
      int offset = 0;
      int nb = 10;
      int eventIndex, start = 0;
      List<IEvent> events;
      boolean hasMore, notFound;
      do {
        if (query == null) {
          events = cal.getEventsInternal(start, nb);
        } else {
          events = cal.searchEvents(query).getEventList(start, nb);
        }
        hasMore = events.size() == nb;
        eventIndex = events.indexOf(event);
        notFound = eventIndex < 0;
        offset = start + eventIndex;
        start = start + nb;
        nb = nb * 2;
        LOGGER.trace("getNavigationDetails: index {} for events '{}'", eventIndex, events);
      } while (notFound && hasMore);
      if (!notFound) {
        navDetail = NavigationDetails.create(cal.getStartDate(), offset);
      } else {
        LOGGER.warn("getNavigationDetails: not found for query [" + query
            + "] and event [" + event + "].");
      }
    } else {
      LOGGER.warn("getNavigationDetails: eventDate is null for '" + event + "'");
    }
    if (navDetail != null) {
      LOGGER.debug("getNavigationDetails: found '{}'", navDetail);
      return navDetail;
    } else {
      throw new NavigationDetailException("Unable to create NavigationDetails for cal '"
          + calDocRef + "', event '" + event + "'");
    }
  }

  private ICalendarService getCalService() {
    if (calService == null) {
      calService = Utils.getComponent(ICalendarService.class);
    }
    return calService;
  }

  void injectCalService(ICalendarService calService) {
    this.calService = calService;
  }

}
