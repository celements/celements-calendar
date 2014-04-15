package com.celements.calendar.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Calendar;
import com.celements.calendar.ICalendar;
import com.celements.calendar.classes.CalendarClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CalendarService implements ICalendarService {

  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calClasses;

  private static Log LOGGER = LogFactory.getFactory().getInstance(CalendarService.class);

  public static final String CALENDAR_SERVICE_START_DATE =
      "com.celements.calendar.service.CalendarService.startDate";

  private static final String EXECUTIONCONTEXT_KEY_CAL_CACHE = "calCache";
  private static final String EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE = "calSpaceCache";

  @Requirement
  private QueryManager queryManager;
  
  @Requirement
  private EntityReferenceResolver<String> referenceResolver;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public List<DocumentReference> getAllCalendars() {
    return getAllCalendars(null, new HashSet<DocumentReference>());
  }

  public List<DocumentReference> getAllCalendars(WikiReference wikiRef) {
    return getAllCalendars(wikiRef, new HashSet<DocumentReference>());
  }

  public List<DocumentReference> getAllCalendars(Collection<DocumentReference> excludes) {
    return getAllCalendars(null, excludes);
  }

  public List<DocumentReference> getAllCalendars(WikiReference wikiRef, 
      Collection<DocumentReference> excludes) {
    if (wikiRef == null) {
      wikiRef = new WikiReference(getContext().getDatabase());
    }
    List<DocumentReference> allCalendars = new ArrayList<DocumentReference>();
    Set<DocumentReference> excludesSet = new HashSet<DocumentReference>(excludes);
    for (DocumentReference calDocRef : getAllCalendarsInternal(wikiRef)) {
      if (!excludesSet.contains(calDocRef)) {
        allCalendars.add(calDocRef);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("getAllCalendars: returned for wiki '" + wikiRef + "' and excludes '" 
          + excludes + "' calendars: " + allCalendars);
    }
    return allCalendars;
  }

  @SuppressWarnings("unchecked")
  List<DocumentReference> getAllCalendarsInternal(WikiReference wikiRef) {
    String contextKey = EXECUTIONCONTEXT_KEY_CAL_CACHE; 
    Map<WikiReference, List<DocumentReference>> calCache; 
    calCache = (Map<WikiReference, List<DocumentReference>>) execution.getContext(
        ).getProperty(contextKey);
    if (calCache == null) {
      calCache = new HashMap<WikiReference, List<DocumentReference>>();
      execution.getContext().setProperty(contextKey, calCache);
    }
    List<DocumentReference> allCalendars = calCache.get(wikiRef);
    if (allCalendars == null) {
      allCalendars = executeAllCalendarsQuery(wikiRef);
      calCache.put(wikiRef, allCalendars);
      LOGGER.debug("getCalsFromCache: put for wiki '" + wikiRef + "': " + allCalendars);
    }
    return allCalendars;
  }
  
  List<DocumentReference> executeAllCalendarsQuery(WikiReference wikiRef) {
    List<DocumentReference> allCalendars = new ArrayList<DocumentReference>();
    try {
      Query query = queryManager.createQuery(getAllCalendarsXWQL(), Query.XWQL);
      query.setWiki(wikiRef.getName());
      for (Object fullName : query.execute()) {
        allCalendars.add(new DocumentReference(referenceResolver.resolve(
            fullName.toString(), EntityType.DOCUMENT, wikiRef)));
      }
    } catch (QueryException exc) {
      LOGGER.error("failed to execute query [" + getAllCalendarsXWQL() + "]", exc);
    }
    return Collections.unmodifiableList(allCalendars);
  }

  String getAllCalendarsXWQL() {
    return "from doc.object(" + CalendarClasses.CALENDAR_CONFIG_CLASS 
        + ") as cal where doc.translation = 0";
  }

  public String getEventSpaceForCalendar(DocumentReference calDocRef
      ) throws XWikiException {
    XWikiDocument doc = getContext().getWiki().getDocument(calDocRef, getContext());
    String space = doc.getDocumentReference().getName();
    BaseObject obj = doc.getXObject(getCalendarClassRef(calDocRef.getWikiReference()));
    if (obj != null) {
      space = obj.getStringValue(CalendarClasses.PROPERTY_CALENDAR_SPACE).trim();
    }
    return space;
  }

  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException {
    List<String> spaces = new ArrayList<String>();
    WikiReference wikiRef = calDocRef.getWikiReference();
    BaseObject calObj = getContext().getWiki().getDocument(calDocRef, getContext()
        ).getXObject(getCalendarClassRef(wikiRef));
    if (calObj != null) {
      addNonEmptyString(spaces, calObj.getStringValue(
          CalendarClasses.PROPERTY_CALENDAR_SPACE));
      spaces.addAll(getSubscribedSpaces(calObj, wikiRef));
    }
    return spaces;
  }

  private List<String> getSubscribedSpaces(BaseObject calObj, WikiReference wikiRef
      ) throws XWikiException {
    List<String> spaces = new ArrayList<String>();
    if (calObj != null) {
      DocumentReference calClassRef = getCalendarClassRef(wikiRef);
      for (Object subDocName : calObj.getListValue(
          CalendarClasses.PROPERTY_SUBSCRIBE_TO)) {
        DocumentReference subDocRef = new DocumentReference(referenceResolver.resolve(
            subDocName.toString(), EntityType.DOCUMENT, wikiRef));
        BaseObject subscCalObj = getContext().getWiki().getDocument(subDocRef,
            getContext()).getXObject(calClassRef);
        if (subscCalObj != null) {
          addNonEmptyString(spaces, subscCalObj.getStringValue(
              CalendarClasses.PROPERTY_CALENDAR_SPACE));
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
    return getCalendarDocRefByCalendarSpace(calSpace, null);
  }

  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace, 
      String inSpace) {
    List<DocumentReference> calConfigs = getCalendarDocRefsByCalendarSpace(calSpace, 
        inSpace);
    if (calConfigs.size() > 0) {
      return calConfigs.get(0);
    } else {
      return null;
    }
  }

  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace) {
    return getCalendarDocRefsByCalendarSpace(calSpace, (SpaceReference) null);
  }

  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace, 
      String inSpace) {
    SpaceReference inSpaceRef = null;
    if (inSpace != null) {
      inSpaceRef = new SpaceReference(inSpace, new WikiReference(getContext(
          ).getDatabase()));
    }
    return getCalendarDocRefsByCalendarSpace(calSpace, inSpaceRef);
  }

  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace, 
      EntityReference inRef) {
    WikiReference inWikiRef;
    if (inRef != null) {
      inWikiRef = (WikiReference) inRef.extractReference(EntityType.WIKI);
    } else {
      inWikiRef = new WikiReference(getContext().getDatabase());
    }
    List<DocumentReference> calDocRefs = getCalSpaceCache(inWikiRef).get(calSpace);
    List<DocumentReference> ret;
    if (calDocRefs == null) {
      ret = new ArrayList<DocumentReference>();
    } else if ((inRef != null) && (inRef.getType() == EntityType.SPACE)) {
        ret = filterForSpaceRef(calDocRefs, (SpaceReference) inRef.extractReference(
            EntityType.SPACE));
    } else {
      ret = new ArrayList<DocumentReference>(calDocRefs);
    }
    LOGGER.trace("getCalendarDocRefsByCalendarSpace: for calSpace '" + calSpace 
        + "' and inRef '" + inRef + "' returned calendars: " + ret);
    return ret;
  }

  @SuppressWarnings("unchecked")
  Map<String, List<DocumentReference>> getCalSpaceCache(WikiReference wikiRef) {
    String contextKey = EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + wikiRef.getName(); 
    Map<String, List<DocumentReference>> calSpaceCache = 
        (Map<String, List<DocumentReference>>) execution.getContext().getProperty(
            contextKey);
    if (calSpaceCache == null) {
      LOGGER.debug("getCalSpaceCache: none found for wiki '" + wikiRef + "', loading now");
      try {
        calSpaceCache = generateCalSpaceMap(wikiRef);
        execution.getContext().setProperty(contextKey, calSpaceCache);
      } catch (XWikiException exc) {
        LOGGER.error("Failed getting calSpaceCache for wiki '" + wikiRef + "'");
        calSpaceCache = new HashMap<String, List<DocumentReference>>();
      }
    } else {
      LOGGER.trace("getCalSpaceCache: got from execution for wiki '" + wikiRef + "'");
    }
    return calSpaceCache;
  }
  
  private Map<String, List<DocumentReference>> generateCalSpaceMap(WikiReference wikiRef
      ) throws XWikiException {
    Map<String, List<DocumentReference>> calSpaceMap = 
        new HashMap<String, List<DocumentReference>>();
    for (DocumentReference calDocRef : getAllCalendars(wikiRef)) {
      String calSpace = getEventSpaceForCalendar(calDocRef);
      if (!calSpaceMap.containsKey(calSpace)) {
        calSpaceMap.put(calSpace, new ArrayList<DocumentReference>());
      }
      LOGGER.trace("generateCalSpaceMap: put for space '" + calSpace + "': " + calDocRef);
      calSpaceMap.get(calSpace).add(calDocRef);
    }
    // since all calendars have been loaded, this map is not allowed to change anymore
    for (String key : new HashSet<String>(calSpaceMap.keySet())) {
      calSpaceMap.put(key, Collections.unmodifiableList(calSpaceMap.remove(key)));
    }
    return Collections.unmodifiableMap(calSpaceMap);
  }
  
  List<DocumentReference> filterForSpaceRef(List<DocumentReference> calDocRefs,
      SpaceReference inSpaceRef) {
    List<DocumentReference> filtered = new ArrayList<DocumentReference>();
    if (inSpaceRef == null) {
      filtered.addAll(calDocRefs);
    } else {
      for (DocumentReference calDocRef : calDocRefs) {        
        if (calDocRef.getLastSpaceReference().equals(inSpaceRef)) {
          filtered.add(calDocRef);
        }
      }
    }
    return filtered;
  }

  public Date getMidnightDate(Date date) {
    Date dateMidnight = null;
    if (date != null) {
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.setTime(date);
      cal.set(java.util.Calendar.HOUR, 0);
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
      cal.set(java.util.Calendar.MINUTE, 0);
      cal.set(java.util.Calendar.SECOND, 0);
      dateMidnight = cal.getTime();
    }
    LOGGER.debug("date is: " + dateMidnight);
    return dateMidnight;
  }
  
  private DocumentReference getCalendarClassRef(WikiReference wikiRef) {
    return ((CalendarClasses) calClasses).getCalendarClassRef(wikiRef.getName());
  }
  
  void injectQueryManager(QueryManager queryManager) {
    this.queryManager = queryManager;
  }
  
  void injectCalCache(Map<WikiReference, List<DocumentReference>> calCache) {
    execution.getContext().setProperty(EXECUTIONCONTEXT_KEY_CAL_CACHE, calCache);
  }
  
  // for test purposes only
  @SuppressWarnings("unchecked")
  Map<WikiReference, List<DocumentReference>> getCalCacheForTests() {
    return (Map<WikiReference, List<DocumentReference>>) execution.getContext(
        ).getProperty(EXECUTIONCONTEXT_KEY_CAL_CACHE);
  }
  
  void injectCalSpaceCache(String database, 
      Map<String, List<DocumentReference>> calSpaceCache) {
    String contextKey = EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + database; 
    execution.getContext().setProperty(contextKey, calSpaceCache);
  }
  
  // for test purposes only
  @SuppressWarnings("unchecked")
  Map<String, List<DocumentReference>> getCalSpaceCacheForTests(String database) {
    return (Map<String, List<DocumentReference>>) execution.getContext(
        ).getProperty(EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + database);
  }

}
