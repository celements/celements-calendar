package com.celements.calendar.tag;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.calendar.ICalendar;
import com.celements.calendar.classes.CalendarClassDefinition;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;

@Singleton
@Component(CalendarTag.CLASS_DEF_HINT)
public class CalendarTag extends AbstractClassDefinition implements CalendarClassDefinition {

  private static final String DOC_NAME = "CalendarTagClass";

  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<ICalendar> FIELD_NAME = new CalendarClassField.Builder(
      CLASS_DEF_HINT, "name").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }

}
