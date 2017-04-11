package com.celements.calendar.bankholidays.classes;

import java.util.Date;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.DateField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Immutable
@Component(BankHolidayClass.CLASS_DEF_HINT)
public class BankHolidayClass extends AbstractClassDefinition implements BankHolidayClassesRole {

  public static final String CLASS_NAME = "BankHolidayClass";
  public static final String CLASS_SPACE = "Classes";
  public static final String CLASS_DEF_HINT = CLASS_SPACE + "." + CLASS_NAME;

  public static final ClassField<String> FIELD_HOLIDAY_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "holiday_name").prettyName("Holiday name").validationRegExp(
          VALIDATION_REGEXP_HOLIDAY_NAME).validationMessage(VALIDATION_MESSAGE_HOLIDAY_NAME).size(
              30).build();

  public static final ClassField<Date> FIELD_HOLIDAY_FROM = new DateField.Builder(CLASS_DEF_HINT,
      "holiday_from").prettyName("Holiday from").dateFormat(DATE_FORMAT_TIMESTAMP).emptyIsToday(
          0).validationRegExp(VALIDATION_REGEXP_DATE_TIME).validationMessage(
              VALIDATION_MESSAGE_DATE_TIME).build();

  public static final ClassField<Date> FIELD_HOLIDAY_TO = new DateField.Builder(CLASS_DEF_HINT,
      "holiday_to").prettyName("Holiday to").dateFormat(DATE_FORMAT_TIMESTAMP).emptyIsToday(
          0).validationRegExp(VALIDATION_REGEXP_DATE_TIME).validationMessage(
              VALIDATION_MESSAGE_DATE_TIME).build();

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
    return CLASS_SPACE;
  }

  @Override
  protected String getClassDocName() {
    return CLASS_NAME;
  }
}
