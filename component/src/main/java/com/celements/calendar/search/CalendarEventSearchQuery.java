package com.celements.calendar.search;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendar;
import com.celements.calendar.ICalendarClassConfig;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class CalendarEventSearchQuery extends DefaultEventSearchQuery implements
    ICalendarSearchQueryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarEventSearchQuery.class);

  private Date startDate = null;
  private boolean isArchive = false;
  private List<String> allowedSpaces = Collections.emptyList();

  public CalendarEventSearchQuery(WikiReference wikiRef) {
    super(wikiRef);
  }

  public CalendarEventSearchQuery(IEventSearchQuery query) {
    super(query, null);
  }

  @Deprecated
  public CalendarEventSearchQuery(ICalendar cal, List<String> sortFields) {
    super(cal.getWikiRef(), getDefaultSortFields(sortFields, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  @Deprecated
  public CalendarEventSearchQuery(ICalendar cal, IEventSearchQuery query) {
    super(query, getDefaultSortFields(null, cal.isArchive()));
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = cal.getAllowedSpaces();
  }

  @Override
  public ICalendarSearchQueryBuilder addCalendarRestrictions(ICalendar cal) {
    checkNotNull(cal);
    this.startDate = cal.getStartDate();
    this.isArchive = cal.isArchive();
    this.allowedSpaces = cal.getAllowedSpaces();
    setSortFields(getDefaultSortFields(getSortFields(), cal.isArchive()));
    return this;
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    return addCalendarRestrictions(query);
  }

  private LuceneQuery addCalendarRestrictions(LuceneQuery query) {
    checkState(this.startDate != null,
        "Calendar must be initialized before calling addCalendarRestrictions");
    query.add(getSearchService().createRestrictionGroup(Type.OR, Arrays.asList("space"),
        getAllowedSpaces()));
    if (!isArchive) {
      query.add(getSearchService().createFromDateRestriction(
          ICalendarClassConfig.CALENDAR_EVENT_CLASS + "."
              + ICalendarClassConfig.PROPERTY_EVENT_DATE,
          startDate, true));
    } else {
      query.add(getSearchService().createToDateRestriction(ICalendarClassConfig.CALENDAR_EVENT_CLASS
          + "." + ICalendarClassConfig.PROPERTY_EVENT_DATE, startDate, false));
    }
    return query;
  }

  private @NotNull List<String> getAllowedSpaces() {
    List<String> ret = new ArrayList<>();
    if (!allowedSpaces.isEmpty()) {
      for (String space : allowedSpaces) {
        ret.add("\"" + space + "\"");
      }
    } else {
      // inexistent space
      ret.add("\".\"");
    }
    return ret;
  }

  @Override
  public String toString() {
    return "CalendarEventSearchQuery [" + super.toString() + ", startDate=" + startDate
        + ", isArchive=" + isArchive + ", allowedSpaces=" + allowedSpaces + "]";
  }

  static List<String> getDefaultSortFields(@Nullable List<String> sortFields, boolean inverted) {
    if ((sortFields == null) || sortFields.isEmpty()) {
      LOGGER.info("getDefaultSortFields: got empty sortFields, using default");
      String pref = (inverted ? "-" : "") + ICalendarClassConfig.CALENDAR_EVENT_CLASS + ".";
      return Arrays.asList(pref + ICalendarClassConfig.PROPERTY_EVENT_DATE, pref
          + ICalendarClassConfig.PROPERTY_EVENT_DATE_END,
          pref
              + ICalendarClassConfig.PROPERTY_TITLE);
    } else {
      return sortFields;
    }
  }

}
