package com.celements.calendar.bankholidays.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(BankHolidayClassPackage.NAME)
public class BankHolidayClassPackage extends AbstractClassPackage {

  public static final String NAME = "bankholidays";

  @Requirement
  private List<BankHolidayClassesRole> classDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDefs);
  }

}
