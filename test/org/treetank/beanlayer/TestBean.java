/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.beanlayer;

public class TestBean {

  @ID
  private long idField;

  private byte[] byteArrayField;

  private boolean booleanField;

  @FullText
  private String stringField;

  private int intField;

  private long longField;

  private float floatField;

  private double doubleField;

  /**
   * @return the idField
   */
  public final long getIdField() {
    return idField;
  }

  /**
   * @param idField the idField to set
   */
  public final void setIdField(final int idField) {
    this.idField = idField;
  }

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
