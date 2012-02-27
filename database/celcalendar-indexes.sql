-- adding the following indexes.

alter table cel_calendareventclass
  add index SORTBYDATE_IDX (CEC_LANG, CEC_DATE, CEC_DATE_END, CEC_TITEL, CEC_ID);
