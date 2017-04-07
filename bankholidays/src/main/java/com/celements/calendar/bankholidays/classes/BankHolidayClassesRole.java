package com.celements.calendar.bankholidays.classes;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.classes.ClassDefinition;

@ComponentRole
public interface BankHolidayClassesRole extends ClassDefinition {

  public static final String DATE_FORMAT_TIMESTAMP = "dd.MM.yyyy HH:mm";
}
