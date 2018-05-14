package com.celements.calendar.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DBListField;
import com.celements.model.classes.fields.number.IntField;
import com.celements.model.classes.fields.ref.SpaceReferenceField;

@Singleton
@Component(CalendarConfigClass.CLASS_DEF_HINT)
public class CalendarConfigClass extends AbstractClassDefinition implements
    CalendarClassDefinition {

  public static final String DOC_NAME = "CalendarConfigClass";
  public static final String CLASS_NAME = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_NAME;

  public static final ClassField<SpaceReference> FIELD_SPACE = new SpaceReferenceField.Builder(
      CLASS_DEF_HINT, "calendarspace").build();

  public static final ClassField<String> FIELD_OVERVIEW_COLUMN_CONFIG = new StringField.Builder(
      CLASS_DEF_HINT, "overview_column_config").size(30).build();

  public static final ClassField<String> FIELD_EVENT_COLUMN_CONFIG = new StringField.Builder(
      CLASS_DEF_HINT, "event_column_config").size(30).build();

  public static final ClassField<Integer> FIELD_EVENT_PER_PAGE = new IntField.Builder(
      CLASS_DEF_HINT, "event_per_page").size(5).build();

  public static final ClassField<Boolean> FIELD_HAS_MORE_LINK = new BooleanField.Builder(
      CLASS_DEF_HINT, "hasMoreLink").displayType("yesno").build();

  public static final ClassField<Boolean> FIELD_IS_SUBSCRIBABLE = new BooleanField.Builder(
      CLASS_DEF_HINT, "is_subscribable").displayType("yesno").build();

  public static final ClassField<List<String>> FIELD_SUBSCRIBE_TO = new DBListField.Builder(
      CLASS_DEF_HINT, "subscribe_to").size(5).multiSelect(true).sql(getSubscribeToHql()).build();

  private static String getSubscribeToHql() {
    return "select doc.fullName from XWikiDocument as doc, BaseObject as obj, IntegerProperty as int "
        + "where obj.name=doc.fullName and not doc.fullName='$doc.getFullName()' "
        + "and obj.className='" + CLASS_NAME + "' and int.id.id=obj.id and int.id.name='"
        + FIELD_IS_SUBSCRIBABLE.getName() + "' and int.value='1' order by doc.fullName asc";
  }

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
