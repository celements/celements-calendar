package com.celements.calendar;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.DocumentReference;

import com.celements.calendar.service.ICalendarService;
import com.celements.marshalling.AbstractMarshaller;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

@Immutable
public class CalendarMarshaller extends AbstractMarshaller<ICalendar> {

  public CalendarMarshaller() {
    super(ICalendar.class);
  }

  @Override
  public String serialize(ICalendar calendar) {
    return calendar.getEventSpaceRef().getName();
  }

  @Override
  public Optional<ICalendar> resolve(String val) {
    ICalendar calendar = null;
    DocumentReference calDocRef = getCalService().getCalendarDocRefByCalendarSpace(val);
    if (calDocRef != null) {
      calendar = getCalService().getCalendar(calDocRef);
    }
    return Optional.fromNullable(calendar);
  }

  private ICalendarService getCalService() {
    return Utils.getComponent(ICalendarService.class);
  }

}
