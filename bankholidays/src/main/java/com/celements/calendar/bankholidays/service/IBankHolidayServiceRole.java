package com.celements.calendar.bankholidays.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.search.lucene.LuceneSearchException;

@ComponentRole
public interface IBankHolidayServiceRole {

  public List<DocumentReference> getBankHolidayRefs(SpaceReference bankHolidaySpaceRef)
      throws LuceneSearchException;
}
