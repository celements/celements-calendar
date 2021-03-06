package com.celements.calendar.search;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xwiki.model.reference.WikiReference;

import com.celements.calendar.ICalendarClassConfig;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class SearchTermEventSearchQuery extends DateEventSearchQuery {

  private final String searchTerm;
  private boolean fuzzy;

  public SearchTermEventSearchQuery(WikiReference wikiRef, Date fromDate, Date toDate,
      String searchTerm, boolean fuzzy, List<String> sortFields) {
    super(wikiRef, fromDate, toDate, sortFields);
    this.searchTerm = searchTerm;
    this.fuzzy = fuzzy;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean isFuzzy() {
    return fuzzy;
  }

  @Override
  protected LuceneQuery getAsLuceneQueryInternal(LuceneQuery query) {
    query = super.getAsLuceneQueryInternal(query);
    if (StringUtils.isNotBlank(searchTerm)) {
      List<String> fields = getSearchTermFields();
      for (String s : searchTerm.split(",")) {
        query.add(getSearchService().createRestrictionGroup(Type.OR, fields, Arrays.asList(s), true,
            fuzzy));
      }
    }
    return query;
  }

  protected List<String> getSearchTermFields() {
    return Arrays.asList(ICalendarClassConfig.CALENDAR_EVENT_CLASS + "."
        + ICalendarClassConfig.PROPERTY_TITLE, ICalendarClassConfig.CALENDAR_EVENT_CLASS + "."
            + ICalendarClassConfig.PROPERTY_DESCRIPTION);
  }

  @Override
  public String toString() {
    return "SearchTermEventSearchQuery [" + super.toString() + ", searchTerm=" + searchTerm
        + ", fuzzy=" + fuzzy + "]";
  }

}
