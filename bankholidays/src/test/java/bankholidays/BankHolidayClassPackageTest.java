package bankholidays;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.calendar.bankholidays.classes.BankHolidayClass;
import com.celements.calendar.bankholidays.classes.BankHolidayClassPackage;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassPackage;
import com.xpn.xwiki.web.Utils;

public class BankHolidayClassPackageTest extends AbstractComponentTest {

  private BankHolidayClassPackage bankHolidayPackage;
  private BankHolidayClass bankHolidayClass;
  private ClassDefinition classDef;

  @Before
  public void prepareTest() {
    bankHolidayPackage = (BankHolidayClassPackage) Utils.getComponent(ClassPackage.class,
        BankHolidayClassPackage.NAME);
    bankHolidayClass = (BankHolidayClass) Utils.getComponent(ClassDefinition.class,
        BankHolidayClass.CLASS_DEF_HINT);
  }

  @Test
  public void testGetClassDefinitions() {
    assertFalse(bankHolidayPackage.getClassDefinitions().isEmpty());
    assertTrue(bankHolidayPackage.getClassDefinitions().contains(bankHolidayClass));
  }

  @Test
  public void testGetClassDefinitions_immutability() {
    bankHolidayPackage.getClassDefinitions().clear();
    assertFalse(bankHolidayPackage.getClassDefinitions().isEmpty());
  }
}
