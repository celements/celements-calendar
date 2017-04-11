package com.celements.calendar.bankholidays.classes;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.classes.ClassDefinition;

@ComponentRole
public interface BankHolidayClassesRole extends ClassDefinition {

  public static final String DATE_FORMAT_TIMESTAMP = "dd.MM.yyyy HH:mm";
  public static final String VALIDATION_REGEXP_DATE_TIME = "/^(0[1-9]|[12][0-9]|3[01])\\."
      + "(0[1-9]|1[012])\\.(19|20|99)\\d\\d (0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/";
  public static final String VALIDATION_MESSAGE_DATE_TIME = "bank_holiday_validation_date_time";
  public static final String VALIDATION_REGEXP_HOLIDAY_NAME = "/^.{0,64}$/";
  public static final String VALIDATION_MESSAGE_HOLIDAY_NAME = "bank_holiday_validation_name";
}
