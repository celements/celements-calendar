package com.celements.calendar.observation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

public class EventDeletingEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor initializing the event filter with an
   * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
   * other document delete event.
   */
  public EventDeletingEvent() {
    super();
  }
  
  /**
   * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
   * meaning that this event will match only delete events affecting the same document.
   * 
   * @param documentReference the reference of the document to match
   */
  public EventDeletingEvent(DocumentReference documentReference) {
    super(documentReference);
  }
  
  /**
   * Constructor using a custom {@link EventFilter}.
   * 
   * @param eventFilter the filter to use for matching events
   */
  public EventDeletingEvent(EventFilter eventFilter) {
    super(eventFilter);
  }
}
