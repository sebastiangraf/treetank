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

  private byte[] byteArrayField;

  private boolean booleanField;

  @FullText
  private String stringField;

  private int intField;

  private long longField;

  private float floatField;

  private double doubleField;

  /**
   * @return the byteArrayField
   */
  public final byte[] getByteArrayField() {
    return byteArrayField;
  }

  /**
   * @param byteArrayField the byteArrayField to set
   */
  public final void setByteArrayField(final byte[] byteArrayField) {
    this.byteArrayField = byteArrayField;
  }

  /**
   * @return the booleanField
   */
  public final boolean isBooleanField() {
    return booleanField;
  }

  /**
   * @param booleanField the booleanField to set
   */
  public final void setBooleanField(final boolean booleanField) {
    this.booleanField = booleanField;
  }

  /**
   * @return the stringField
   */
  public final String getStringField() {
    return stringField;
  }

  /**
   * @param stringField the stringField to set
   */
  public final void setStringField(final String stringField) {
    this.stringField = stringField;
  }

  /**
   * @return the intField
   */
  public final int getIntField() {
    return intField;
  }

  /**
   * @param intField the intField to set
   */
  public final void setIntField(final int intField) {
    this.intField = intField;
  }

  /**
   * @return the longField
   */
  public final long getLongField() {
    return longField;
  }

  /**
   * @param longField the longField to set
   */
  public final void setLongField(final long longField) {
    this.longField = longField;
  }

  /**
   * @return the floatField
   */
  public final float getFloatField() {
    return floatField;
  }

  /**
   * @param floatField the floatField to set
   */
  public final void setFloatField(final float floatField) {
    this.floatField = floatField;
  }

  /**
   * @return the doubleField
   */
  public final double getDoubleField() {
    return doubleField;
  }

  /**
   * @param doubleField the doubleField to set
   */
  public final void setDoubleField(final double doubleField) {
    this.doubleField = doubleField;
  }

}
