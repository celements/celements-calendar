package com.celements.calendar.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.calendar.ICalendar;
import com.celements.calendar.classes.fields.CalendarClassField;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;

@Singleton
@Component(CalendarTagClass.CLASS_DEF_HINT)
public class CalendarTagClass extends AbstractClassDefinition implements CalendarClassDefinition {

  public static final String DOC_NAME = "CalendarTagClass";
  public static final String CLASS_NAME = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_NAME;

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
