package com.celements.calendar;

import org.xwiki.model.reference.DocumentReference;

public class CalendarCreateException extends Exception {

  private static final long serialVersionUID = 5337374851309383630L;

  public CalendarCreateException(DocumentReference docRef) {
    super(getMessage(docRef));
  }

  public CalendarCreateException(DocumentReference docRef, Throwable cause) {
    super(getMessage(docRef), cause);
  }

  private static String getMessage(DocumentReference docRef) {
    return "Unable to create Calendar for doc '" + docRef + "'";
  }

}
