package com.celements.calendar.classes;

import java.util.Date;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.DateField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(CalendarEventClass.CLASS_DEF_HINT)
public class CalendarEventClass extends AbstractClassDefinition implements CalendarClassDefinition {

  public static final String DOC_NAME = "CalendarConfigClass";
  public static final String CLASS_NAME = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_NAME;

  public static final ClassField<String> FIELD_LANG = new StringField.Builder(CLASS_DEF_HINT,
      "lang").size(30).build();

  public static final ClassField<String> FIELD_TITLE = new StringField.Builder(CLASS_DEF_HINT,
      "l_title").size(30).build();

  public static final ClassField<String> FIELD_TITLE_RTE = new LargeStringField.Builder(
      CLASS_DEF_HINT, "l_title_rte").rows(15).size(80).build();

  public static final ClassField<String> FIELD_DESCRIPTION = new LargeStringField.Builder(
      CLASS_DEF_HINT, "l_description").rows(15).size(80).build();

  public static final ClassField<String> FIELD_LOCATION = new StringField.Builder(CLASS_DEF_HINT,
      "location").size(30).build();

  public static final ClassField<String> FIELD_LOCATION_RTE = new LargeStringField.Builder(
      CLASS_DEF_HINT, "location_rte").rows(15).size(80).build();

  public static final ClassField<Date> FIELD_EVENT_DATE = new DateField.Builder(CLASS_DEF_HINT,
      "eventDate").size(20).emptyIsToday(0).validationRegExp(getRegexDate(false,
          true)).validationMessage("cel_calendar_validation_event_date").build();

  public static final ClassField<Date> FIELD_EVENT_END_DATE = new DateField.Builder(CLASS_DEF_HINT,
      "eventDate_end").size(20).emptyIsToday(0).validationRegExp(getRegexDate(true,
          true)).validationMessage("cel_calendar_validation_event_end_date").build();

  public static final ClassField<Boolean> FIELD_IS_SUBSCRIBABLE = new BooleanField.Builder(
      CLASS_DEF_HINT, "isSubscribable").displayType("yesno").build();

  public static String getRegexDate(boolean allowEmpty, boolean withTime) {
    String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
    if (withTime) {
      regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9])";
    }
    return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
  }

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
