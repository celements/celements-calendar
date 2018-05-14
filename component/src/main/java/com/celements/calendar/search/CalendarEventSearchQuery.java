package com.celements.calendar.search;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.SpaceReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.calendar.tag.CalendarTagClass;
import com.celements.model.classes.ClassDefinition;
import com.celements.search.lucene.LuceneUtils;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.web.Utils;

public class CalendarEventSearchQuery extends DefaultEventSearchQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEventSearchQuery.class);

  private final Date startDate;
  private final boolean isArchive;
  private final List<SpaceReference> allowedSpaces;

  public CalendarEventSearchQuery(ICalendar cal, List<String> sortFields) {
    super(cal.getWikiRef(), getDefaultSortFields(sortFields, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = checkNotNull(cal.getAllowedSpaceRefs());
    checkArgument(!allowedSpaces.isEmpty());
  }

  public CalendarEventSearchQuery(ICalendar cal, IEventSearchQuery query) {
    super(query, getDefaultSortFields(null, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = checkNotNull(cal.getAllowedSpaceRefs());
    checkArgument(!allowedSpaces.isEmpty());
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    for (SpaceReference spaceRef : allowedSpaces) {
      if (isTaggingMode()) {
        query.add(getSearchService().createFieldRestriction(
            getCalendarTagClassDef().getClassReference().getDocRef(),
            CalendarTagClass.FIELD_NAME.getName(), LuceneUtils.exactify(spaceRef)));
      } else {
        query.add(getSearchService().createSpaceRestriction(spaceRef));
      }
    }
    if (!isArchive) {
      query.add(getSearchService().createFromDateRestriction(
          ICalendarClassConfig.CALENDAR_EVENT_CLASS + "."
              + ICalendarClassConfig.PROPERTY_EVENT_DATE, startDate, true));
    } else {
      query.add(getSearchService().createToDateRestriction(ICalendarClassConfig.CALENDAR_EVENT_CLASS
          + "." + ICalendarClassConfig.PROPERTY_EVENT_DATE, startDate, false));
    }
    return query;
  }

  private boolean isTaggingMode() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String toString() {
    return "CalendarEventSearchQuery [" + super.toString() + ", startDate=" + startDate
        + ", isArchive=" + isArchive + ", allowedSpaces=" + allowedSpaces + "]";
  }

  private ClassDefinition getCalendarTagClassDef() {
    return Utils.getComponent(ClassDefinition.class, CalendarTagClass.CLASS_DEF_HINT);
  }

  static List<String> getDefaultSortFields(List<String> sortFields, boolean inverted) {
    if ((sortFields == null) || sortFields.isEmpty()) {
      LOGGER.info("getDefaultSortFields: got empty sortFields, using default");
      String pref = (inverted ? "-" : "") + ICalendarClassConfig.CALENDAR_EVENT_CLASS + ".";
      return Arrays.asList(pref + ICalendarClassConfig.PROPERTY_EVENT_DATE, pref
          + ICalendarClassConfig.PROPERTY_EVENT_DATE_END, pref
              + ICalendarClassConfig.PROPERTY_TITLE);
    } else {
      return sortFields;
    }
  }

}
