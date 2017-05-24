package com.celements.calendar.bankholidays.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.calendar.bankholidays.classes.BankHolidayClass;
import com.celements.calendar.bankholidays.classes.BankHolidayClassesRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;

@Component
public class BankHolidayService implements IBankHolidayServiceRole {

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement(BankHolidayClass.CLASS_DEF_HINT)
  private BankHolidayClassesRole bankHolidayClass;

  @Override
  public List<DocumentReference> getBankHolidayRefs(SpaceReference bankHolidaySpaceRef)
      throws LuceneSearchException {
    DocumentReference bankHolidayClassRef = bankHolidayClass.getClassRef();
    LuceneQuery query = searchService.createQuery();
    query.add(searchService.createSpaceRestriction(bankHolidaySpaceRef));
    query.add(searchService.createObjectRestriction(bankHolidayClassRef));
    LuceneSearchResult result = searchService.search(query, null, null);
    return result.getResults(DocumentReference.class);
  }

}
