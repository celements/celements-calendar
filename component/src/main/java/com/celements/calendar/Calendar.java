/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.calendar;

import static com.celements.calendar.classes.CalendarConfigClass.*;
import static com.celements.model.util.References.*;
import static com.google.common.base.MoreObjects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.api.EventApi;
import com.celements.calendar.classes.CalendarConfigClass;
import com.celements.calendar.classes.CalendarEventClass;
import com.celements.calendar.engine.ICalendarEngineRole;
import com.celements.calendar.manager.IEventManager;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearchQuery;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class Calendar implements ICalendar {

  private static final Logger LOGGER = LoggerFactory.getLogger(Calendar.class);

  private static final String OVERVIEW_DEFAULT_CONFIG = "date,time,l_title,location";
  private static final String DETAILVIEW_DEFAULT_CONFIG = "date,time,l_title,location,l_description";
  private static final Set<String> NON_EVENT_PROPERTYS = ImmutableSet.of(
      CalendarEventClass.FIELD_LANG.getName(), CalendarEventClass.FIELD_IS_SUBSCRIBABLE.getName(),
      CalendarEventClass.FIELD_EVENT_DATE.getName(),
      CalendarEventClass.FIELD_EVENT_END_DATE.getName());

  private final XWikiDocument calDoc;
  private Date startDate;
  private boolean isArchive;
  private String defaultLang;
  private String language;
  private ICalendarEngineRole engine;

  public Calendar(DocumentReference calDocRef) throws CalendarCreateException {
    this(calDocRef, false);
  }

  public Calendar(DocumentReference calDocRef, boolean isArchive) throws CalendarCreateException {
    this(loadAndVerifyDoc(calDocRef), isArchive);
  }

  Calendar(XWikiDocument calDoc, boolean isArchive) {
    this.calDoc = calDoc;
    this.isArchive = isArchive;
    this.startDate = new Date();
  }

  private static XWikiDocument loadAndVerifyDoc(DocumentReference calDocRef)
      throws CalendarCreateException {
    try {
      XWikiDocument calDoc = getModelAccess().getDocument(calDocRef);
      if (createObjectFetcher(calDoc).exists()) {
        return calDoc;
      } else {
        throw new CalendarCreateException(calDocRef);
      }
    } catch (DocumentNotExistsException dne) {
      throw new CalendarCreateException(calDocRef, dne);
    }
  }

  @Override
  public DocumentReference getDocumentReference() {
    return calDoc.getDocumentReference();
  }

  @Override
  public WikiReference getWikiRef() {
    return extractRef(getDocumentReference(), WikiReference.class).get();
  }

  @Override
  public XWikiDocument getCalDoc() {
    return calDoc;
  }

  @Override
  public boolean isArchive() {
    return isArchive;
  }

  @Override
  public Date getStartDate() {
    return this.startDate;
  }

  @Override
  public ICalendar setStartTimestamp(Date newStartDate) {
    if (newStartDate != null) {
      this.startDate = newStartDate;
    }
    return this;
  }

  @Override
  public ICalendar setStartDate(Date newStartDate) {
    return setStartTimestamp(DateUtil.noTime(newStartDate));
  }

  @Override
  public String getLanguage() {
    return firstNonNull(language, getDefaultLang());
  }

  @Override
  public ICalendar setLanguage(String language) {
    this.language = language;
    return this;
  }

  private final String getDefaultLang() {
    if (defaultLang == null) {
      defaultLang = getContext().getDefaultLanguage(getEventSpaceRef());
    }
    return defaultLang;
  }

  @Override
  public SpaceReference getEventSpaceRef() { // TODO check calls, they are likely to be faulty
    SpaceReference spaceRef = getModelAccess().getFieldValue(getConfigObject(), FIELD_SPACE).or(
        getDefaultSpaceRef());
    LOGGER.debug("getEventSpaceRef: got '{}' for cal '{}'", spaceRef, getDocumentReference());
    return spaceRef;
  }

  private SpaceReference getDefaultSpaceRef() {
    return getModelUtils().resolveRef(getDocumentReference().getName(), SpaceReference.class,
        getWikiRef());
  }

  @Override
  @Deprecated
  public List<String> getAllowedSpaces() {
    List<String> spaces = new ArrayList<>();
    for (SpaceReference spaceRef : getAllowedSpaceRefs()) {
      spaces.add(spaceRef.getName());
    }
    return spaces;
  }

  @Override
  public List<SpaceReference> getAllowedSpaceRefs() {
    List<SpaceReference> spaces = new ArrayList<>();
    spaces.add(getEventSpaceRef());
    spaces.addAll(getSubscribedSpaces());
    return spaces;
  }

  private List<SpaceReference> getSubscribedSpaces() {
    List<SpaceReference> spaces = new ArrayList<>();
    for (String subDocName : getModelAccess().getFieldValue(getConfigObject(),
        FIELD_SUBSCRIBE_TO).get()) {
      try {
        ICalendar subsCal = new Calendar(getModelUtils().resolveRef(subDocName.toString(),
            DocumentReference.class, getWikiRef()), isArchive());
        spaces.add(subsCal.getEventSpaceRef());
      } catch (CalendarCreateException exc) {
        LOGGER.warn("getSubscribedSpaces - invalid subscriber", exc);
      }
    }
    return spaces;
  }

  @Override
  public boolean addEvent(DocumentReference eventDocRef) throws DocumentNotExistsException,
      DocumentSaveException {
    return getEventManager().addEvent(this, eventDocRef);
  }

  @Override
  public boolean removeEvent(DocumentReference eventDocRef) throws DocumentNotExistsException,
      DocumentSaveException {
    return getEventManager().removeEvent(this, eventDocRef);
  }

  @Deprecated
  @Override
  public List<EventApi> getAllEvents() {
    return getEventManager().getEvents(this, 0, 0);
  }

  @Override
  public List<IEvent> getAllEventsInternal() {
    return getEventManager().getAllEventsInternal(this);
  }

  @Override
  @Deprecated
  public List<EventApi> getEvents(int start, int nb) {
    return getEventManager().getEvents(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
  }

  @Override
  public List<IEvent> getEventsInternal(int start, int nb) {
    return getEventManager().getEventsInternal(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
  }

  @Override
  public EventSearchResult searchEvents(IEventSearchQuery query) {
    return getEventManager().searchEvents(this, query);
  }

  @Override
  public long getNrOfEvents() {
    return getEventManager().countEvents(this);
  }

  @Override
  public boolean isEmpty() {
    return getNrOfEvents() == 0;
  }

  @Override
  public IEvent getFirstEvent() {
    return getEventManager().getFirstEvent(this);
  }

  @Override
  public IEvent getLastEvent() {
    return getEventManager().getLastEvent(this);
  }

  @Override
  public List<String> getOverviewFields() {
    return Arrays.asList(getOverviewConfig().split(","));
  }

  private String getOverviewConfig() {
    String overviewConfig = getModelAccess().getFieldValue(getConfigObject(),
        FIELD_OVERVIEW_COLUMN_CONFIG).or(OVERVIEW_DEFAULT_CONFIG);
    LOGGER.debug("getOverviewConfig: '{}'", overviewConfig);
    return overviewConfig;
  }

  @Override
  public List<String> getDetailviewFields() {
    return Arrays.asList(getDetailviewConfig().split(","));
  }

  private String getDetailviewConfig() {
    String detailviewConfig = getModelAccess().getFieldValue(getConfigObject(),
        FIELD_EVENT_COLUMN_CONFIG).or(DETAILVIEW_DEFAULT_CONFIG);
    LOGGER.debug("getDetailviewConfig: '{}'", detailviewConfig);
    return detailviewConfig;
  }

  @Override
  public List<String> getCalOverviewPropertyNames() {
    return getCalOverviewPropertyNames_internal();
  }

  /**
   * @deprecated use getCalOverviewPropertyNames() instead
   */
  @Deprecated
  @Override
  public List<String> getCalOverviewPropertyNames(XWikiContext context) {
    return getCalOverviewPropertyNames_internal();
  }

  private List<String> getCalOverviewPropertyNames_internal() {
    List<String> propNames = getEventPropertyNames();
    if (hasDetailLink()) {
      propNames.add("detaillink");
    }
    return propNames;
  }

  @Override
  public List<String> getEventPropertyNames() {
    return getEventPropertyNames_internal();
  }

  /**
   * @deprecated use getEventPropertyNames() instead
   */
  @Deprecated
  @Override
  public List<String> getEventPropertyNames(XWikiContext context) {
    return getEventPropertyNames_internal();
  }

  private List<String> getEventPropertyNames_internal() {
    List<String> propNames = new ArrayList<>();
    try {
      BaseClass bclass = getModelAccess().getDocument(
          getCalendarConfigClass().getClassReference().getDocRef(getWikiRef())).getXClass();
      for (String propertyName : bclass.getPropertyNames()) {
        if (!NON_EVENT_PROPERTYS.contains(propertyName)) {
          propNames.add(propertyName);
        }
      }
      propNames.add("date");
      propNames.add("time");
      propNames.add("date_end");
      propNames.add("time_end");
    } catch (DocumentNotExistsException dne) {
      LOGGER.error("Event Class Document not available", dne);
    }
    return propNames;
  }

  @Override
  public boolean hasDetailLink() {
    return getModelAccess().getFieldValue(getConfigObject(), FIELD_HAS_MORE_LINK).or(false);
  }

  @Override
  public boolean isSubscribable() {
    return getModelAccess().getFieldValue(getConfigObject(), FIELD_IS_SUBSCRIBABLE).or(false);
  }

  @Override
  public ICalendarEngineRole getEngine() {
    if (engine == null) {
      ICalendarEngineRole defaultEngine = Utils.getComponent(ICalendarEngineRole.class);
      engine = getEngineWithoutLimitCheck();
      if (!StringUtils.equals(engine.getName(), defaultEngine.getName())) {
        long limit = engine.getEngineLimit();
        long size = engine.countEvents(this);
        if ((limit != 0) && (size >= limit)) {
          engine = defaultEngine;
        }
        LOGGER.debug("getEngine: size {}, limit {}", size, limit);
      }
      LOGGER.debug("getEngine: returning '{}' for  cal '{}'", engine.getName(),
          getDocumentReference());
    }
    return engine;
  }

  @Override
  public ICalendarEngineRole getEngineWithoutLimitCheck() {
    String engineHint = getContext().getXWikiContext().getWiki().getXWikiPreference(
        "calendar_engine", "calendar.engine", "default", getContext().getXWikiContext());
    return Utils.getComponent(ICalendarEngineRole.class, engineHint);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getDocumentReference()).append(startDate).append(
        isArchive).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Calendar) {
      Calendar other = (Calendar) obj;
      return new EqualsBuilder().append(this.getDocumentReference(),
          other.getDocumentReference()).append(this.startDate, other.startDate).append(
              this.isArchive, other.isArchive).isEquals();
    }
    return false;
  }

  protected BaseObject getConfigObject() {
    return createObjectFetcher(getCalDoc()).first().get();
  }

  private static XWikiObjectFetcher createObjectFetcher(XWikiDocument calDoc) {
    return XWikiObjectEditor.on(calDoc).fetch().filter(getCalendarConfigClass());
  }

  protected static ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

  protected static IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  protected static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  protected static ClassDefinition getCalendarConfigClass() {
    return Utils.getComponent(ClassDefinition.class, CalendarConfigClass.CLASS_DEF_HINT);
  }

  protected static IEventManager getEventManager() {
    return Utils.getComponent(IEventManager.class);
  }

  protected static IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  @Override
  public String toString() {
    return "Calendar [calDocRef=" + getModelUtils().serializeRef(getDocumentReference())
        + ", startDate=" + startDate + ", isArchive=" + isArchive + ", language=" + getLanguage()
        + ", allowedSpaces=" + getAllowedSpaces() + ", engine=" + getEngine().getName() + "]";
  }

}
