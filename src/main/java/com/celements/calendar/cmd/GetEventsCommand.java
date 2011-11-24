package com.celements.calendar.cmd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.calendar.Event;
import com.celements.calendar.ICalendar;
import com.celements.calendar.api.EventApi;
import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.calendar.util.CalendarUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetEventsCommand {

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      GetEventsCommand.class);

  public GetEventsCommand() {}

  public List<EventApi> getEvents(XWikiDocument calDoc, int start, int nb,
      boolean isArchive, XWikiContext context) throws XWikiException {
    String query = getQuery(calDoc, isArchive, false, context);
    List<EventApi> eventList = new ArrayList<EventApi>();
    try {
      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> eventDocs = storage.search(query, nb, start, context);
      mLogger.debug(eventDocs.size() + " events found. " + eventDocs);
      for (String eventDocName : eventDocs) {
        mLogger.debug(eventDocName);
        if(checkEventSubscription(calDoc, eventDocName, context)){
          eventList.add(new EventApi(new Event(eventDocName, context), context));
        }
      }
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    return eventList;
  }

  public long countEvents(XWikiDocument calDoc, boolean isArchive, XWikiContext context) {
    List<Object> eventCount = null;
    try {
      eventCount = context.getWiki().getStore().search(getQuery(calDoc, isArchive, true,
          context), 0, 0, context);
    } catch (XWikiException e) {
      mLogger.error("Exception while counting number of events for calendar '" + 
          ((calDoc != null)?calDoc.getDocumentReference():calDoc) + "'", e);
    }
    if((eventCount != null) && (eventCount.size() > 0)) {
      mLogger.debug("Count resulted in " + eventCount.get(0) + " which is of class " +
          eventCount.get(0).getClass());
      return (Long)eventCount.get(0);
    }
    return 0;
  }
  
  private String getQuery(XWikiDocument calDoc, boolean isArchive, boolean count,
      XWikiContext context) throws XWikiException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    String timeComp = ">=";
    String sortOrder = "asc";
    String selectEmptyDates = "or ec.eventDate is null";
    if(isArchive){
      timeComp = "<";
      sortOrder = "desc";
      selectEmptyDates = "";
    }
    String hql = "select ";
    if(count){
      hql += "count(obj.name)";
    } else {
      hql += "obj.name";
    }
    hql += " from XWikiDocument doc, BaseObject as obj, ";
    hql += CelementsCalendarPlugin.CLASS_EVENT + " as ec ";
    hql += "where doc.fullName = obj.name and doc.translation = 0 and ec.id.id=obj.id ";
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    String defaultLanguage = (String)vcontext.get("default_language");
    hql += "and ec.lang='" + defaultLanguage + "' ";
    hql += "and (ec.eventDate " + timeComp + " '"
      + format.format(getMidnightDate()) + "' " + selectEmptyDates + ") and ";
    hql += CalendarUtils.getInstance().getAllowedSpacesHQL(calDoc, context);
    hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
    mLogger.debug(hql);
    
    return hql;
  }
  
  private Date getMidnightDate() {
    return getMidnightDate(new Date());
  }

  /**
   * getMidnightDate
   * 
   * @param startDate may not be null
   * @return
   */
  private Date getMidnightDate(Date startDate) {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(startDate);
    cal.set(java.util.Calendar.HOUR, 0);
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    Date dateMidnight = cal.getTime();
    mLogger.debug("date is: " + dateMidnight);
    return dateMidnight;
  }
  
  private boolean checkEventSubscription(XWikiDocument calDoc, String eventDocName,
      XWikiContext context) throws XWikiException {
    return isHomeCalendar(calDoc, eventDocName, context)
        || isEventSubscribed(calDoc, eventDocName, context);
  }

  private boolean isHomeCalendar(XWikiDocument calDoc, String eventDocName,
      XWikiContext context) throws XWikiException {
    return eventDocName.startsWith(CalendarUtils.getInstance().getEventSpaceForCalendar(
        calDoc, context) + ".");
  }
  
  private boolean isEventSubscribed(XWikiDocument calDoc, String eventDocName,
      XWikiContext context) throws XWikiException {
    Event event = new Event(eventDocName, context);
    BaseObject obj = event.getEventDocument().getObject(
        CelementsCalendarPlugin.SUBSCRIPTION_CLASS, "subscriber", calDoc.getFullName(),
        false);

    ICalendar calendar = event.getCalendar(context);
    BaseObject calObj = null;
    if ((calendar != null) && (calendar.getCalDoc() != null)){
      calObj = calendar.getCalDoc().getObject("Classes.CalendarConfigClass");
    }
    boolean isSubscribed = false;
    if((obj != null) && (obj.getIntValue("doSubscribe") == 1)
        && (calObj != null) && (calObj.getIntValue("is_subscribable") == 1)){
      isSubscribed = true;
    }
    return isSubscribed;
  }
  
}
