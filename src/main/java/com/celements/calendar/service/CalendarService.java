package com.celements.calendar.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CalendarService implements ICalendarService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(CalendarService.class);

  public static final String CLASS_CALENDAR_SPACE = "Classes";
  public static final String CLASS_CALENDAR_DOC = "CalendarConfigClass";
  public static final String CLASS_CALENDAR = CLASS_CALENDAR_SPACE + "." + CLASS_CALENDAR_DOC;
  public static final String PROPERTY_IS_SUBSCRIBABLE = "is_subscribable";
  public static final String PROPERTY_EVENT_PER_PAGE = "event_per_page";
  public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = "overview_column_config";
  public static final String PROPERTY_EVENT_COLUMN_CONFIG = "event_column_config";
  public static final String PROPERTY_HAS_MORE_LINK = "hasMoreLink";
  public static final String PROPERTY_SUBSCRIBE_TO = "subscribe_to";
  public static final String PROPERTY_CALENDAR_SPACE = "calendarspace";

  public static final String SUBSCRIPTION_CLASS_SPACE = "Classes";
  public static final String SUBSCRIPTION_CLASS_DOC = "SubscriptionClass";
  public static final String SUBSCRIPTION_CLASS = SUBSCRIPTION_CLASS_SPACE + "." + SUBSCRIPTION_CLASS_DOC;

  public static final String CALENDAR_SERVICE_START_DATE =
      "com.celements.calendar.service.CalendarService.startDate";

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private EntityReferenceResolver<String> stringRefResolver;

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public String getEventSpaceForCalendar(DocumentReference calDocRef
      ) throws XWikiException {
    XWikiDocument doc = getContext().getWiki().getDocument(calDocRef, getContext());
    String space = doc.getDocumentReference().getName();
    BaseObject obj = doc.getXObject(getCalendarConfigReference());
    if (obj != null) {
      space = obj.getStringValue(PROPERTY_CALENDAR_SPACE).trim();
    }
    return space;
  }

  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException {
    List<String> spaces = new ArrayList<String>();
    BaseObject calObj = getContext().getWiki().getDocument(calDocRef, getContext()
        ).getXObject(getCalendarConfigReference());
    if (calObj != null) {
      addNonEmptyString(spaces, calObj.getStringValue(PROPERTY_CALENDAR_SPACE));
      spaces.addAll(getSubscribedSpaces(calObj));
    }
    return spaces;
  }

  private List<String> getSubscribedSpaces(BaseObject calObj) throws XWikiException {
    List<String> spaces = new ArrayList<String>();
    if (calObj != null) {
      DocumentReference calConfRef = getCalendarConfigReference();
      for (Object subDocName : calObj.getListValue(PROPERTY_SUBSCRIBE_TO)) {
        DocumentReference subDocRef = webUtils.resolveDocumentReference(
            subDocName.toString());
        BaseObject subscCalObj = getContext().getWiki().getDocument(subDocRef,
            getContext()).getXObject(calConfRef);
        if (subscCalObj != null) {
          addNonEmptyString(spaces, subscCalObj.getStringValue(PROPERTY_CALENDAR_SPACE));
        }
      }
    }
    return spaces;
  }

  private void addNonEmptyString(List<String> list, String str) {
    str = str.trim();
    if ((str != null) && (str.length() > 0)) {
      list.add(str);
    }
  }

  @Deprecated
  public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException {
    String spaceHQL = "";
    List<String> spaces = getAllowedSpaces(calDoc.getDocumentReference());
    for (String space : spaces) {
      if (spaceHQL.length() > 0) {
        spaceHQL += " or ";
      }
      spaceHQL += "obj.name like '" + space + ".%'";
    }
    if (spaceHQL.length() > 0) {
      spaceHQL = "(" + spaceHQL + ")";
    } else {
      spaceHQL = "(obj.name like '.%')";
    }
    return spaceHQL;
  }

  public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
    LOGGER.trace("getCalendarByCalRef: create Calendar reference for [" + calDocRef
        + "], isArchive [" + isArchive + "].");
    return new Calendar(calDocRef, isArchive);
  }

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
    String xwql = "from doc.object(Classes.CalendarConfigClass) as calConfig";
    xwql += " where calConfig.calendarspace = :calSpace";
    Query query;
    try {
      query = queryManager.createQuery(xwql, Query.XWQL);
      query.bindValue("calSpace", calSpace);
      List<String> blogList = query.execute();
      if(blogList.size() > 0){
        DocumentReference calDocRef = new DocumentReference(stringRefResolver.resolve(
            blogList.get(0), EntityType.DOCUMENT));
        calDocRef.setWikiReference(new WikiReference(getContext().getDatabase()));
        return calDocRef;
      } else {
        LOGGER.error("getCalendarDocRefByCalendarSpace: no calendar found for space ["
            + calSpace + "].");
      }
    } catch (QueryException exp) {
      LOGGER.error("getCalendarDocRefByCalendarSpace: failed to execute XWQL [" + xwql
          + "].", exp);
    }
    return null;
  }

  private DocumentReference getCalendarConfigReference() {
    return new DocumentReference(getContext().getDatabase(), CLASS_CALENDAR_SPACE,
        CLASS_CALENDAR_DOC);
  }

}
