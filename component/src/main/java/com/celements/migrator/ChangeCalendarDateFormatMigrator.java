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
   * migration 06.08.2014 -> 1678
   * http://www.convertunits.com/dates/from/Jan+1,+2010/to/Aug+7,+2014
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1668);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(((CalendarClasses) calendarClasses
        ).getCalendarEventClassRef(context.getDatabase()), context);
    BaseClass bClass = doc.getXClass();
    DateClass eventDateElement = (DateClass) bClass.get("eventDate");
    DateClass eventEndDateElement = (DateClass) bClass.get("eventDate_end");
    eventDateElement.setDateFormat("dd.MM.yyyy HH:mm");
    eventDateElement.setValidationRegExp(
        "/^((0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4}) " +
        "([01][0-9]|2[0-4])(\\:[0-5][0-9]))$/");
    eventEndDateElement.setValidationMessage("cel_blog_validation_archivedate");
    eventEndDateElement.setDateFormat("dd.MM.yyyy HH:mm");
    eventDateElement.setValidationRegExp(
        "/(^$)|^((0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4}) " +
        "([01][0-9]|2[0-4])(\\:[0-5][0-9]))$/");
    context.getWiki().saveDocument(doc, context);
  }
  
}