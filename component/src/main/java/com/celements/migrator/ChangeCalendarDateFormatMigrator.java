package com.celements.migrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.query.QueryManager;

import com.celements.calendar.classes.CalendarClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("ChangeCalendarDateFormatMigrator")
public class ChangeCalendarDateFormatMigrator extends AbstractCelementsHibernateMigrator {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      ChangeCalendarDateFormatMigrator.class);
  
  @Requirement
  private QueryManager queryManager;
  
  @Requirement("celements.CalendarClasses")
  private IClassCollectionRole calendarClasses;
  
  @Requirement
  private IWebUtilsService webUtilsService;

  public String getName() {
    return "ChangeCalendarDateFormatMigrator";
  }

  public String getDescription() {
    return "Change the DateFormat";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this
   * migration 06.08.2014 -> 1684
   * http://www.convertunits.com/dates/from/Jan+1,+2010/to/Aug+12,+2014
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1684);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(((CalendarClasses) calendarClasses
        ).getCalendarEventClassRef(context.getDatabase()), context);
    BaseClass bClass = doc.getXClass();
    addRegexToEventDateField(bClass);
    addRegexToEventEndDateField(bClass);
    context.getWiki().saveDocument(doc, context);
  }

  private void addRegexToEventDateField(BaseClass bClass) {
    DateClass eventDateElement = (DateClass) bClass.get(
        CalendarClasses.PROPERTY_EVENT_DATE);
    eventDateElement.setValidationMessage(CalendarClasses.PROPERTY_EVENT_DATE_VALIDATION);
    eventDateElement.setDateFormat(CalendarClasses.PROPERTY_EVENT_DATE_FORMAT);
    eventDateElement.setValidationRegExp(((CalendarClasses) calendarClasses
        ).getRegexDate(false, true));
  }
  
  private void addRegexToEventEndDateField(BaseClass bClass) {
    DateClass eventEndDateElement = (DateClass) bClass.get(
        CalendarClasses.PROPERTY_EVENT_DATE_END);
    eventEndDateElement.setValidationMessage(
        CalendarClasses.PROPERTY_EVENT_DATE_END_VALIDATION);
    eventEndDateElement.setDateFormat(CalendarClasses.PROPERTY_EVENT_DATE_FORMAT);
    eventEndDateElement.setValidationRegExp(((CalendarClasses) calendarClasses
        ).getRegexDate(true, true));
  }

}