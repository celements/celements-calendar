package com.celements.calendar.navigation;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.CalendarNavigationFactory;
import com.celements.calendar.navigation.factories.ICalendarNavigationFactory;
import com.celements.calendar.navigation.factories.INavigationDetailsFactory;
import com.celements.calendar.navigation.factories.NavigationDetailException;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.navigation.factories.NavigationDetailsFactory;
import com.celements.calendar.search.DateEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.web.service.EditorSupportScriptService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CalendarNavigationService implements ICalendarNavigationService {

  private INavigationDetailsFactory navDetailsFactory;
  private ICalendarNavigationFactory calNavFactory;

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarNavigationService.class);

  public NavigationDetails getNavigationDetails(Date startDate, int offset
      ) throws NavigationDetailException {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(startDate, offset);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event) throws NavigationDetailException {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(calConfigDocRef, event);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, IEventSearchQuery query) throws NavigationDetailException, LuceneSearchException {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(calConfigDocRef, event, query);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb) {
    LOGGER.debug("called getCalendarNavigation");
    return getCalNavFactory().getCalendarNavigation(calConfigDocRef, navDetails, nb);
  }

  /**
   * @deprecated instead use {@link #getCalendarNavigation(DocumentReference,
   *  NavigationDetails, int, DateEventSearchQuery, boolean) and specify if a empty
   *  return page is needed or not
   */
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query) 
          throws LuceneSearchException {
    LOGGER.debug("called getCalendarNavigation");
    return getCalNavFactory().getCalendarNavigation(calDocRef, navDetails, nb, query, 
        false);
  }
  
  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query,
      boolean isSendingEmptyPage) throws LuceneSearchException {
    LOGGER.debug("called getCalendarNavigation");
    return getCalNavFactory().getCalendarNavigation(calDocRef, navDetails, nb, query, 
        isSendingEmptyPage);
  }

  private INavigationDetailsFactory getNavDetailsFactory() {
    if (navDetailsFactory == null) {
      navDetailsFactory = new NavigationDetailsFactory();
    }
    return navDetailsFactory;
  }

  void injectNavDetailsFactory(INavigationDetailsFactory navDetailsFactory) {
    this.navDetailsFactory = navDetailsFactory;
  }

  private ICalendarNavigationFactory getCalNavFactory() {
    if (calNavFactory == null) {
      calNavFactory = new CalendarNavigationFactory();
    }
    return calNavFactory;
  }

  void injectCalNavFactory(ICalendarNavigationFactory calNavFactory) {
    this.calNavFactory = calNavFactory;
  }

}
