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
 * $Id$
 */

package org.treetank.pagelayer;

import org.treetank.api.IPage;
import org.treetank.api.IWriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>AbstractPage</h1>
 * 
 * <p>
 * A collection of commonly used page tools to reduce code clutter and
 * stability.
 * </p>
 */
public abstract class AbstractPage implements IPage {

  /**
   * Initialize given page reference with virgin page reference.
   */
  public static final PageReference createPageReference() {
    return new PageReference();
  }

  /**
   * Initialize given page reference array with virgin page references.
   * 
   * @param references Page reference array to initialize.
   */
  public static final void createPageReferences(final PageReference[] references) {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = createPageReference();
    }
  }

  /**
   * Deserialize page reference from reader to page reference.
   * 
   * @param references Page reference array to deserialize.
   * @param in Reader to read from.
   * @throws Exception of any kind.
   */
  public static final PageReference readPageReference(
      final FastByteArrayReader in) throws Exception {
    return new PageReference(in);
  }

  /**
   * Deserialize page references from reader to page reference array.
   * 
   * @param references Page reference array to deserialize.
   * @param in Reader to read from.
   * @throws Exception of any kind.
   */
  public static final void readPageReferences(
      final PageReference[] references,
      final FastByteArrayReader in) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = readPageReference(in);
    }
  }

  /**
   * COW committed page reference to virgin page reference.
   * 
   * @param committedReference Committed page reference to COW.
   */
  public static final PageReference clonePageReference(
      final PageReference committedReference) {
    return new PageReference(committedReference);
  }

  /**
   * COW committed page reference array to virgin page reference array.
   * 
   * @param references Virgin page reference array.
   * @param committedReferences Committed page reference array to COW.
   */
  public static final void clonePageReferences(
      final PageReference[] references,
      final PageReference[] committedReferences) {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = clonePageReference(committedReferences[i]);
    }
  }

  /**
   * Safely serialize referenced page.
   * 
   * @param out FastByteArrayWriter to write page to.
   * @param reference Reference to dereference and serialize.
   * @throws Exception of any kind.
   */
  public static final void serialize(
      final FastByteArrayWriter out,
      final PageReference reference) throws Exception {
    reference.serialize(out);
  }

  /**
   * Safely serialize referenced pages.
   * 
   * @param out FastByteArrayWriter to write page to.
   * @param references Reference array to dereference and serialize.
   * @throws Exception of any kind.
   */
  public static final void serialize(
      final FastByteArrayWriter out,
      final PageReference[] references) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      serialize(out, references[i]);
    }
  }

  /**
   * {@inheritDoc}
   */
  public abstract void commit(final IWriteTransactionState state)
      throws Exception;

  /**
   * {@inheritDoc}
   */
  public abstract void serialize(FastByteArrayWriter out) throws Exception;

}
