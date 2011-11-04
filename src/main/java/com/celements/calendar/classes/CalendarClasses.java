package com.celements.calendar.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;

import com.celements.calendar.plugin.CelementsCalendarPlugin;
import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.CalendarClasses")
public class CalendarClasses extends CelementsClassCollection {

  private static Log mLogger = LogFactory.getFactory().getInstance(CalendarClasses.class);

  @Override
  protected Log getLogger() {
    return mLogger;
  }

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    getCalendarClass(context);
    getCalendarEventClass(context);
    getSubscriptionClass(context);
  }

  public String getConfigName() {
    return "celCalendar";
  }

  private BaseClass getCalendarClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(CelementsCalendarPlugin.CLASS_CALENDAR, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(CelementsCalendarPlugin.CLASS_CALENDAR_SPACE);
      doc.setName(CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(CelementsCalendarPlugin.CLASS_CALENDAR);
    needsUpdate |= bclass.addTextField(CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE,
        CelementsCalendarPlugin.PROPERTY_CALENDAR_SPACE, 30);
    String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj,";
    hql += " IntegerProperty as int ";
    hql += "where obj.name=doc.fullName ";
    hql += "and not doc.fullName='$doc.getFullName()' ";
    hql += "and obj.className='" + CelementsCalendarPlugin.CLASS_CALENDAR + "' ";
    hql += "and int.id.id=obj.id ";
    hql += "and int.id.name='" + CelementsCalendarPlugin.PROPERTY_IS_SUBSCRIBABLE + "' ";
    hql += "and int.value='1' ";
    hql += "order by doc.fullName asc";
    needsUpdate |= bclass.addDBListField(CelementsCalendarPlugin.PROPERTY_SUBSCRIBE_TO,
        CelementsCalendarPlugin.PROPERTY_SUBSCRIBE_TO, 5, true, hql);
    needsUpdate |= bclass.addTextField(
        CelementsCalendarPlugin.PROPERTY_OVERVIEW_COLUMN_CONFIG,
        CelementsCalendarPlugin.PROPERTY_OVERVIEW_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addTextField(
        CelementsCalendarPlugin.PROPERTY_EVENT_COLUMN_CONFIG,
        CelementsCalendarPlugin.PROPERTY_EVENT_COLUMN_CONFIG, 30);
    needsUpdate |= bclass.addNumberField(CelementsCalendarPlugin.PROPERTY_EVENT_PER_PAGE,
        CelementsCalendarPlugin.PROPERTY_EVENT_PER_PAGE, 5, "integer");
    needsUpdate |= bclass.addBooleanField(CelementsCalendarPlugin.PROPERTY_HAS_MORE_LINK,
        CelementsCalendarPlugin.PROPERTY_HAS_MORE_LINK, "yesno");
    needsUpdate |= bclass.addBooleanField(
        CelementsCalendarPlugin.PROPERTY_IS_SUBSCRIBABLE,
        CelementsCalendarPlugin.PROPERTY_IS_SUBSCRIBABLE, "yesno");
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
  }
  
  private BaseClass getCalendarEventClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(CelementsCalendarPlugin.CLASS_EVENT, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(CelementsCalendarPlugin.CLASS_EVENT_SPACE);
      doc.setName(CelementsCalendarPlugin.CLASS_EVENT_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(CelementsCalendarPlugin.CLASS_EVENT);
    needsUpdate |= bclass.addTextField(CelementsCalendarPlugin.PROPERTY_LANG,
        CelementsCalendarPlugin.PROPERTY_LANG, 30);
    needsUpdate |= bclass.addTextField(CelementsCalendarPlugin.PROPERTY_TITLE,
        CelementsCalendarPlugin.PROPERTY_TITLE, 30);
    needsUpdate |= bclass.addTextAreaField(CelementsCalendarPlugin.PROPERTY_TITLE_RTE,
        CelementsCalendarPlugin.PROPERTY_TITLE_RTE, 80, 15);
    needsUpdate |= bclass.addTextAreaField(CelementsCalendarPlugin.PROPERTY_DESCRIPTION,
        CelementsCalendarPlugin.PROPERTY_DESCRIPTION, 80, 15);
    needsUpdate |= bclass.addTextField(CelementsCalendarPlugin.PROPERTY_LOCATION,
        CelementsCalendarPlugin.PROPERTY_LOCATION, 30);
    needsUpdate |= bclass.addTextAreaField(CelementsCalendarPlugin.PROPERTY_LOCATION_RTE,
        CelementsCalendarPlugin.PROPERTY_LOCATION_RTE, 80, 15);
    needsUpdate |= bclass.addDateField(CelementsCalendarPlugin.PROPERTY_EVENT_DATE,
        CelementsCalendarPlugin.PROPERTY_EVENT_DATE, null, 0);
    needsUpdate |= bclass.addDateField(CelementsCalendarPlugin.PROPERTY_EVENT_DATE_END,
        CelementsCalendarPlugin.PROPERTY_EVENT_DATE_END, null, 0);
    needsUpdate |= bclass.addBooleanField(
        CelementsCalendarPlugin.PROPERTY_EVENT_IS_SUBSCRIBABLE,
        CelementsCalendarPlugin.PROPERTY_EVENT_IS_SUBSCRIBABLE, "yesno");
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
  }
  
  private BaseClass getSubscriptionClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument(CelementsCalendarPlugin.SUBSCRIPTION_CLASS, context);
    } catch (Exception e) {
      doc = new XWikiDocument();
      doc.setSpace(CelementsCalendarPlugin.SUBSCRIPTION_CLASS_SPACE);
      doc.setName(CelementsCalendarPlugin.SUBSCRIPTION_CLASS_DOC);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(CelementsCalendarPlugin.SUBSCRIPTION_CLASS);
    needsUpdate |= bclass.addTextField("subscriber", "subscriber", 30);
    needsUpdate |= bclass.addBooleanField("doSubscribe", "doSubscribe", "yesno");
    
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    return bclass;
  }
  
}
