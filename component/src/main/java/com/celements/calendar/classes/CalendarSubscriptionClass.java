package com.celements.calendar.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(CalendarSubscriptionClass.CLASS_DEF_HINT)
public class CalendarSubscriptionClass extends AbstractClassDefinition implements
    CalendarClassDefinition {

  public static final String DOC_NAME = "SubscriptionClass";
  public static final String CLASS_NAME = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_NAME;

  public static final ClassField<String> FIELD_SUBSCRIBER = new StringField.Builder(CLASS_DEF_HINT,
      "subscriber").size(30).build();

  public static final ClassField<Boolean> FIELD_DO_SUBSCRIBE = new BooleanField.Builder(
      CLASS_DEF_HINT, "doSubscribe").displayType("yesno").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
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
