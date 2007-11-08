/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ParentAxisTest.java 3396 2007-11-05 12:43:35Z kramis $
 */

package org.treetank.beanlayer;

public class TestBean {

  @FullText
  private String token;

  private long date;
  
  private int naturalNumber;
  
  private byte[] byteArray;

  /**
   * @return the token
   */
  public final String getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public final void setToken(final String token) {
    this.token = token;
  }

  /**
   * @return the date
   */
  public final long getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public final void setDate(final long date) {
    this.date = date;
  }

  /**
   * @return the naturalNumber
   */
  public final int getNaturalNumber() {
    return naturalNumber;
  }

  /**
   * @param naturalNumber the naturalNumber to set
   */
  public final void setNaturalNumber(int naturalNumber) {
    this.naturalNumber = naturalNumber;
  }

  /**
   * @return the byteArray
   */
  public final byte[] getByteArray() {
    return byteArray;
  }

  /**
   * @param byteArray the byteArray to set
   */
  public final void setByteArray(final byte[] byteArray) {
    this.byteArray = byteArray;
  }

}
