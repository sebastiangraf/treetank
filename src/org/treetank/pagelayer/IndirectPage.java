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

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.api.IWriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

final public class IndirectPage implements IPage {

  /** True if page was created or cloned. False if it was read or committed. */
  private boolean mDirty;

  private final PageReference[] mIndirectPageReferences;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private IndirectPage(final boolean dirty) {
    mDirty = dirty;
    mIndirectPageReferences = new PageReference[IConstants.INP_REFERENCE_COUNT];
  }

  /**
   * Create new uncommitted in-memory indirect page.
   * 
   * @return Freshly created indirect page.
   */
  public static final IndirectPage create() {
    final IndirectPage indirectPage = new IndirectPage(true);
    return indirectPage;
  }

  /**
   * Read committed indirect page from disk.
   * 
   * @param in Input stream.
   * @return Indirect page read from storage.
   */
  public static final IndirectPage read(final FastByteArrayReader in) {
    final IndirectPage indirectPage = new IndirectPage(false);

    for (int i = 0, l = indirectPage.mIndirectPageReferences.length; i < l; i++) {
      if (in.readBoolean()) {
        indirectPage.mIndirectPageReferences[i] = new PageReference(in);
      }
    }
    return indirectPage;
  }

  /**
   * Clone indirect page.
   * 
   * @param committedIndirectPage Existing page to clone.
   * @return Cloned indirect page.
   */
  public static final IndirectPage clone(
      final IndirectPage committedIndirectPage) {
    final IndirectPage indirectPage = new IndirectPage(true);

    for (int i = 0, l = indirectPage.mIndirectPageReferences.length; i < l; i++) {
      if (committedIndirectPage.mIndirectPageReferences[i] != null) {
        indirectPage.mIndirectPageReferences[i] =
            new PageReference(committedIndirectPage.mIndirectPageReferences[i]);
      }
    }

    return indirectPage;
  }

  /**
   * Get reference by page offset.
   * 
   * @param offset Offset of referenced page.
   * @return Reference at given offset.
   */
  public final PageReference getPageReference(final int offset) {
    if (mIndirectPageReferences[offset] == null) {
      mIndirectPageReferences[offset] = new PageReference();
    }
    return mIndirectPageReferences[offset];
  }

  /**
   * Set page reference at page offset.
   * 
   * @param offset Offset of referenced page.
   * @param reference Page reference to set at pageOffset.
   */
  public final void setPageReference(
      final int offset,
      final PageReference reference) {
    mIndirectPageReferences[offset] = reference;
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final IWriteTransactionState state) throws Exception {
    state.commit(mIndirectPageReferences);
    mDirty = false;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) {
    for (int i = 0, l = mIndirectPageReferences.length; i < l; i++) {
      if (mIndirectPageReferences[i] != null) {
        out.writeBoolean(true);
        mIndirectPageReferences[i].serialize(out);
      } else {
        out.writeBoolean(false);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isDirty() {
    return mDirty;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString() + ": isDirty=" + mDirty;
  }

}
