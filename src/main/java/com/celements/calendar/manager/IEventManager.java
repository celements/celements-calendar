package com.celements.calendar.manager;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.calendar.api.EventApi;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IEventManager {

  public List<EventApi> getEvents(XWikiDocument calDoc, int start, int nb,
      boolean isArchive) throws XWikiException;

  public long countEvents(XWikiDocument calDoc, boolean isArchive);

}
