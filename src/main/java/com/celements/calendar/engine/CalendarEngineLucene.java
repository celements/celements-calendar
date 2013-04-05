package com.celements.calendar.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.calendar.Event;
import com.celements.calendar.IEvent;
import com.celements.calendar.search.EventSearchResult;
import com.celements.calendar.search.IEventSearch;
import com.celements.search.lucene.IQueryService;
import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;

@Component("lucene")
public class CalendarEngineLucene implements ICalendarEngineRole {

	private static final Log LOGGER = LogFactory.getFactory().getInstance(
			CalendarEngineLucene.class);

	@Requirement
	private IQueryService queryService;

	@Requirement
	private IEventSearch eventSearch;

	public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces) {
		return getEventSearchResult(isArchive, startDate, lang, allowedSpaces).getEventList();
	}

	public List<IEvent> getEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces, int offset, int limit) {
		return getEventSearchResult(isArchive, startDate, lang, allowedSpaces).getEventList(
				offset, limit);
	}

	public long countEvents(Date startDate, boolean isArchive, String lang,
			List<String> allowedSpaces) {
		return getEventSearchResult(isArchive, startDate, lang, allowedSpaces).getSize();
	}

	private EventSearchResult getEventSearchResult(boolean isArchive, Date startDate,
			String lang, List<String> allowedSpaces) {
		LuceneQueryApi query = queryService.createQuery();
		addLangRestriction(query, lang);
		addSpaceRestrictions(query, allowedSpaces);
		EventSearchResult searchResult;
		if (!isArchive) {
			searchResult = eventSearch.getSearchResultFromDate(query, startDate);
		} else {
			searchResult = eventSearch.getSearchResultUptoDate(query, startDate);
		}
		LOGGER.debug("getEventSearchResult: " + searchResult);
		return searchResult;
	}

	private void addSpaceRestrictions(LuceneQueryApi query, List<String> allowedSpaces) {
		if (!allowedSpaces.isEmpty()) {
			List<LuceneQueryRestrictionApi> spaceRestrictionList =
					new ArrayList<LuceneQueryRestrictionApi>();
			for (String space : allowedSpaces) {
				spaceRestrictionList.add(queryService.createRestriction("space", space));
			}
			query.addOrRestrictionList(spaceRestrictionList);
		}
	}

	private void addLangRestriction(LuceneQueryApi query, String lang) {
		LuceneQueryRestrictionApi langRestriction = queryService.createRestriction(
				Event.CLASS + "." + Event.PROPERTY_LANG, lang);
		query.addRestriction(langRestriction);
	}

	void injectQueryService(IQueryService queryService) {
		this.queryService = queryService;
	}

	void injectEventSearch(IEventSearch eventSearch) {
		this.eventSearch = eventSearch;
	}

}
