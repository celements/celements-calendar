package com.celements.calendar.navigation;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.IEvent;
import com.celements.calendar.navigation.factories.CalendarNavigation;
import com.celements.calendar.navigation.factories.CalendarNavigationFactory;
import com.celements.calendar.navigation.factories.ICalendarNavigationFactory;
import com.celements.calendar.navigation.factories.INavigationDetailsFactory;
import com.celements.calendar.navigation.factories.NavigationDetails;
import com.celements.calendar.navigation.factories.NavigationDetailsFactory;
import com.celements.calendar.search.DateEventSearchQuery;
import com.celements.calendar.search.IEventSearchQuery;
import com.xpn.xwiki.XWikiContext;

@Component("default")
public class CalendarNavigationService implements ICalendarNavigationService {

  private INavigationDetailsFactory navDetailsFactory;
  private ICalendarNavigationFactory calNavFactory;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarNavigationService.class);

  public NavigationDetails getNavigationDetails(Date startDate, int offset) {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(startDate, offset);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event) {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(calConfigDocRef, event);
  }

  public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
      IEvent event, IEventSearchQuery query) {
    LOGGER.debug("called getNavigationDetails");
    return getNavDetailsFactory().getNavigationDetails(calConfigDocRef, event, query);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calConfigDocRef,
      NavigationDetails navDetails, int nb) {
    LOGGER.debug("called getCalendarNavigation");
    return getCalNavFactory().getCalendarNavigation(calConfigDocRef, navDetails, nb);
  }

  public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
      NavigationDetails navDetails, int nb, DateEventSearchQuery query) {
    LOGGER.debug("called getCalendarNavigation");
    return getCalNavFactory().getCalendarNavigation(calDocRef, navDetails, nb, query);
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
      calNavFactory = new CalendarNavigationFactory(getContext());
    }
    return calNavFactory;
  }

  void injectCalNavFactory(ICalendarNavigationFactory calNavFactory) {
    this.calNavFactory = calNavFactory;
  }

}
