package com.celements.calendar.navigation.factories;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class NavigationDetails {

  private final Date startDate;
  private final int offset;

  private NavigationDetails(Date startDate, int offset) {
    this.startDate = startDate;
    this.offset = offset;
  }

  static NavigationDetails create(Date startDate, int offset) throws NavigationDetailException {
    if ((startDate != null) && (offset >= 0)) {
      return new NavigationDetails(startDate, offset);
    }
    throw new NavigationDetailException("Unable to create nav for startDate '" + startDate
        + "' and offset '" + offset + "'");
  }

  public Date getStartDate() {
    return new Date(startDate.getTime());
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NavigationDetails) {
      NavigationDetails other = (NavigationDetails) obj;
      return new EqualsBuilder().append(this.startDate, other.startDate).append(this.offset,
          other.offset).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(startDate).append(offset).toHashCode();
  }

  @Override
  public String toString() {
    return "NavigationDetails [startDate=" + startDate + ", offset=" + offset + "]";
  }

}
