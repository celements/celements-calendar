package com.celements.calendar.tag;

import javax.validation.constraints.NotNull;

import com.celements.calendar.CalendarMarshaller;
import com.celements.calendar.ICalendar;
import com.celements.marshalling.Marshaller;
import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public class CalendarClassField extends AbstractClassField<ICalendar> implements
    CustomClassField<ICalendar> {

  private static final Marshaller<ICalendar> MARSHALLER = new CalendarMarshaller();

  public static class Builder extends AbstractClassField.Builder<Builder, ICalendar> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public CalendarClassField build() {
      return new CalendarClassField(getThis());
    }

  }

  protected CalendarClassField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<ICalendar> getType() {
    return ICalendar.class;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    return new StringClass();
  }

  @Override
  public Object serialize(ICalendar calendar) {
    if (calendar != null) {
      return MARSHALLER.serialize(calendar);
    }
    return null;
  }

  @Override
  public ICalendar resolve(Object obj) {
    if (obj != null) {
      return MARSHALLER.resolve(obj.toString()).orNull();
    }
    return null;
  }

}
