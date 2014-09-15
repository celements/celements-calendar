package com.celements.calendar.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class CalendarEventSearchQuery extends DefaultEventSearchQuery {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CalendarEventSearchQuery.class);

  private final Date startDate;
  private final boolean isArchive;
  private final String lang;
  private final List<String> allowedSpaces;
  
  public CalendarEventSearchQuery(String database, Date startDate, boolean isArchive, 
      String lang, List<String> allowedSpaces, List<String> sortFields, 
      boolean skipChecks) {
    super(database, getDefaultSortFields(sortFields, isArchive), skipChecks);
    this.startDate = startDate;
    this.isArchive = isArchive;
    this.lang = lang;
    this.allowedSpaces = allowedSpaces;
  }
  
  public CalendarEventSearchQuery(IEventSearchQuery query, Date startDate,
      boolean isArchive, String lang, List<String> allowedSpaces) {
    super(query, getDefaultSortFields(null, isArchive));
    this.startDate = startDate;
    this.isArchive = isArchive;
    this.lang = lang;
    this.allowedSpaces = allowedSpaces;
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    query.add(getSearchService().createRestrictionGroup(Type.OR, Arrays.asList("space"), 
        getAllowedSpaces()));
    query.add(getSearchService().createFieldRestriction(getCalEventClassRef(), 
        CalendarClasses.PROPERTY_LANG, "\"" + lang + "\""));
    if (!isArchive) {
      query.add(getSearchService().createFromDateRestriction(
          CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE,
          startDate, true));
    } else {
      query.add(getSearchService().createToDateRestriction(
          CalendarClasses.CALENDAR_EVENT_CLASS + "." + CalendarClasses.PROPERTY_EVENT_DATE,
          startDate, false));
    }
    return query;
  }

  private List<String> getAllowedSpaces() {
    List<String> ret = new ArrayList<String>();
    if (allowedSpaces != null && allowedSpaces.size() > 0) {
      for(String space : allowedSpaces) {
        ret.add("\"" + space + "\"");
      }
    } else {
      // is there a nicer way to avoid returning events if there are no allowed spaces?
      // if an empty list is returned here instead, then all events will be listed!
      ret.add("\"NullSpace\"");
    }
    return ret;
  }

  @Override
  public String toString() {
    return "CalendarEventSearchQuery [" + super.toString() + ", startDate=" + startDate 
        + ", isArchive=" + isArchive + ", lang=" + lang + ", allowedSpaces=" 
        + allowedSpaces + "]";
  }

  static List<String> getDefaultSortFields(List<String> sortFields, boolean inverted) {
    if (sortFields == null || sortFields.isEmpty()) {
      LOGGER.info("getDefaultSortFields: got empty sortFields, using default");
      String pref = (inverted ? "-" : "") + CalendarClasses.CALENDAR_EVENT_CLASS + ".";
      return Arrays.asList(pref + CalendarClasses.PROPERTY_EVENT_DATE,
          pref + CalendarClasses.PROPERTY_EVENT_DATE_END,
          pref + CalendarClasses.PROPERTY_TITLE);
    } else {
      return sortFields;
    }
  }

}
