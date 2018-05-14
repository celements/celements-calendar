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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.calendar.Calendar;
import com.celements.calendar.CalendarCreateException;
import com.celements.calendar.DateUtil;
import com.celements.calendar.ICalendar;
import com.celements.calendar.classes.CalendarConfigClass;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CalendarService implements ICalendarService {

  private static Logger LOGGER = LoggerFactory.getLogger(CalendarService.class);

  public static final String CALENDAR_SERVICE_START_DATE = "com.celements.calendar.service.CalendarService.startDate";

  static final String EXECUTIONCONTEXT_KEY_CAL_CACHE = "calCache";
  static final String EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE = "calSpaceCache";

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IQueryExecutionServiceRole queryExecutor;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement
  private Execution execution;

  @Override
  public ICalendar createCalendar(DocumentReference calDocRef) throws CalendarCreateException {
    return new Calendar(calDocRef);
  }

  @Override
  public ICalendar createCalendarArchive(DocumentReference calDocRef)
      throws CalendarCreateException {
    return new Calendar(calDocRef, true);
  }

  @Override
  @Deprecated
  public ICalendar getCalendar(DocumentReference calDocRef) {
    try {
      return createCalendar(calDocRef);
    } catch (CalendarCreateException exc) {
      throw new IllegalArgumentException(exc);
    }
  }

  @Override
  @Deprecated
  public ICalendar getCalendar(DocumentReference calDocRef, Date startDate) {
    return getCalendar(calDocRef).setStartDate(startDate);
  }

  @Override
  @Deprecated
  public ICalendar getCalendarArchive(DocumentReference calDocRef) {
    try {
      return createCalendarArchive(calDocRef);
    } catch (CalendarCreateException exc) {
      throw new IllegalArgumentException(exc);
    }
  }

  @Override
  @Deprecated
  public ICalendar getCalendarArchive(DocumentReference calDocRef, Date startDate) {
    return getCalendarArchive(calDocRef).setStartDate(startDate);
  }

  @Deprecated
  @Override
  public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
    LOGGER.trace("getCalendarByCalRef: create Calendar reference for [" + calDocRef
        + "], isArchive [" + isArchive + "].");
    return isArchive ? getCalendarArchive(calDocRef) : getCalendar(calDocRef);
  }

  @Override
  public List<DocumentReference> getAllCalendars() {
    return getAllCalendars(null, new HashSet<DocumentReference>());
  }

  @Override
  public List<DocumentReference> getAllCalendars(WikiReference wikiRef) {
    return getAllCalendars(wikiRef, new HashSet<DocumentReference>());
  }

  @Override
  public List<DocumentReference> getAllCalendars(Collection<DocumentReference> excludes) {
    return getAllCalendars(null, excludes);
  }

  @Override
  public List<DocumentReference> getAllCalendars(WikiReference wikiRef,
      Collection<DocumentReference> excludes) {
    wikiRef = modelUtils.extractRef(wikiRef, WikiReference.class).or(context.getWikiRef());
    List<DocumentReference> allCalendars = new ArrayList<>();
    Set<DocumentReference> excludesSet = new HashSet<>(excludes);
    for (DocumentReference calDocRef : getAllCalendarsInternal(wikiRef)) {
      if (!excludesSet.contains(calDocRef)) {
        allCalendars.add(calDocRef);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("getAllCalendars: returned for wiki '" + wikiRef + "' and excludes '" + excludes
          + "' calendars: " + allCalendars);
    }
    return allCalendars;
  }

  @SuppressWarnings("unchecked")
  List<DocumentReference> getAllCalendarsInternal(WikiReference wikiRef) {
    String contextKey = EXECUTIONCONTEXT_KEY_CAL_CACHE;
    Map<WikiReference, List<DocumentReference>> calCache;
    calCache = (Map<WikiReference, List<DocumentReference>>) execution.getContext().getProperty(
        contextKey);
    if (calCache == null) {
      calCache = new HashMap<>();
      execution.getContext().setProperty(contextKey, calCache);
    }
    List<DocumentReference> allCalendars = calCache.get(wikiRef);
    if (allCalendars == null) {
      allCalendars = executeAllCalendarsQuery(wikiRef);
      calCache.put(wikiRef, allCalendars);
      LOGGER.debug("getAllCalendarsInternal: put to cache for key '" + wikiRef + "': "
          + allCalendars);
    }
    return allCalendars;
  }

  List<DocumentReference> executeAllCalendarsQuery(WikiReference wikiRef) {
    List<DocumentReference> allCalendars;
    try {
      Query query = queryManager.createQuery(getAllCalendarsXWQL(), Query.XWQL);
      query.setWiki(wikiRef.getName());
      allCalendars = Collections.unmodifiableList(queryExecutor.executeAndGetDocRefs(query));
    } catch (QueryException exc) {
      LOGGER.error("failed to execute query [" + getAllCalendarsXWQL() + "]", exc);
      allCalendars = Collections.emptyList();
    }
    return allCalendars;
  }

  String getAllCalendarsXWQL() {
    return "from doc.object(" + CalendarConfigClass.CLASS_NAME
        + ") as cal where doc.translation = 0";
  }

  @Override
  @Deprecated
  public String getEventSpaceForCalendar(DocumentReference calDocRef) throws XWikiException {
    return getEventSpaceRefForCalendar(calDocRef).getName();
  }

  @Override
  @Deprecated
  public SpaceReference getEventSpaceRefForCalendar(DocumentReference calDocRef)
      throws XWikiException {
    try {
      return createCalendar(calDocRef).getEventSpaceRef();
    } catch (CalendarCreateException exc) {
      throw new XWikiException(0, 0, "wrapper", exc);
    }
  }

  @Override
  @Deprecated
  public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException {
    List<String> spaces = new ArrayList<>();
    try {
      for (SpaceReference spaceRef : createCalendar(calDocRef).getAllowedSpaceRefs()) {
        spaces.add(spaceRef.getName());
      }
    } catch (CalendarCreateException dne) {
      LOGGER.warn("getAllowedSpaces:", dne);
    }
    return spaces;
  }

  @Deprecated
  @Override
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

  @Override
  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
    return getCalendarDocRefByCalendarSpace(calSpace, (EntityReference) null);
  }

  /**
   * @Deprecated instead use {@link #getCalendarDocRefByCalendarSpace(String,
   *             EntityReference)}
   */
  @Deprecated
  @Override
  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace, String inSpace) {
    return getCalendarDocRefByCalendarSpace(calSpace, modelUtils.resolveRef(inSpace,
        SpaceReference.class));
  }

  @Override
  public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace,
      EntityReference inRef) {
    return Iterables.getFirst(getCalendarDocRefsByCalendarSpace(calSpace, inRef), null);
  }

  @Override
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace) {
    return getCalendarDocRefsByCalendarSpace(calSpace, (SpaceReference) null);
  }

  @Deprecated
  @Override
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace,
      String inSpace) {
    return getCalendarDocRefsByCalendarSpace(calSpace, modelUtils.resolveRef(inSpace,
        SpaceReference.class));
  }

  // TODO check calls, might be buggy
  @Override
  public List<DocumentReference> getCalendarDocRefsByCalendarSpace(String calSpace,
      EntityReference inRef) {
    WikiReference inWikiRef = modelUtils.extractRef(inRef, WikiReference.class).or(
        context.getWikiRef());
    List<DocumentReference> calDocRefs = getCalSpaceCache(inWikiRef).get(calSpace);
    List<DocumentReference> ret;
    if (calDocRefs == null) {
      ret = new ArrayList<>();
    } else if ((inRef != null) && (inRef.getType() == EntityType.SPACE)) {
      ret = filterForSpaceRef(calDocRefs, (SpaceReference) inRef.extractReference(
          EntityType.SPACE));
    } else {
      ret = new ArrayList<>(calDocRefs);
    }
    LOGGER.trace("getCalendarDocRefsByCalendarSpace: for calSpace '" + calSpace + "' and inRef '"
        + inRef + "' returned calendars: " + ret);
    return ret;
  }

  @SuppressWarnings("unchecked")
  Map<String, List<DocumentReference>> getCalSpaceCache(WikiReference wikiRef) {
    String contextKey = EXECUTIONCONTEXT_KEY_CAL_SPACE_CACHE + "|" + wikiRef.getName();
    Map<String, List<DocumentReference>> calSpaceCache = (Map<String, List<DocumentReference>>) execution.getContext().getProperty(
        contextKey);
    if (calSpaceCache == null) {
      LOGGER.debug("getCalSpaceCache: none found for wiki '" + wikiRef + "', loading now");
      try {
        calSpaceCache = generateCalSpaceMap(wikiRef);
        execution.getContext().setProperty(contextKey, calSpaceCache);
      } catch (XWikiException exc) {
        LOGGER.error("Failed getting calSpaceCache for wiki '" + wikiRef + "'");
        calSpaceCache = new HashMap<>();
      }
    } else {
      LOGGER.trace("getCalSpaceCache: got from execution for wiki '" + wikiRef + "'");
    }
    return calSpaceCache;
  }

  private Map<String, List<DocumentReference>> generateCalSpaceMap(WikiReference wikiRef)
      throws XWikiException {
    Map<String, List<DocumentReference>> calSpaceMap = new HashMap<>();
    for (DocumentReference calDocRef : getAllCalendars(wikiRef)) {
      String calSpace = getEventSpaceForCalendar(calDocRef);
      if (!calSpaceMap.containsKey(calSpace)) {
        calSpaceMap.put(calSpace, new ArrayList<DocumentReference>());
      }
      LOGGER.trace("generateCalSpaceMap: put for space '" + calSpace + "': " + calDocRef);
      calSpaceMap.get(calSpace).add(calDocRef);
    }
    // since all calendars have been loaded, this map is not allowed to change anymore
    for (String key : new HashSet<>(calSpaceMap.keySet())) {
      calSpaceMap.put(key, Collections.unmodifiableList(calSpaceMap.remove(key)));
    }
    return Collections.unmodifiableMap(calSpaceMap);
  }

  List<DocumentReference> filterForSpaceRef(List<DocumentReference> calDocRefs,
      SpaceReference inSpaceRef) {
    List<DocumentReference> filtered = new ArrayList<>();
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

  @Override
  @Deprecated
  public boolean isMidnightDate(Date date) {
    if (date != null) {
      return DateUtil.noTime(date).equals(date);
    }
    return false;
  }

  @Override
  @Deprecated
  public Date getMidnightDate(Date date) {
    return DateUtil.noTime(date);
  }

  @Override
  @Deprecated
  public Date getEndOfDayDate(Date date) {
    return DateUtil.endOfDay(date);
  }

}
