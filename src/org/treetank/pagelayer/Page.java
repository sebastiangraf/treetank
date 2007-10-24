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
 * $Id: IndirectPage.java 3204 2007-10-23 16:19:49Z kramis $
 */

package org.treetank.pagelayer;

import java.io.IOException;

import org.treetank.sessionlayer.WriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>Page</h1>
 * 
 * <p>
 * Class to provide basic reference handling functionality.
 * </p>
 */
public class Page {

  /** Page references. */
  private final PageReference[] mReferences;

  /** True if page was created or cloned. False if it was read or committed. */
  private boolean mDirty;

  /**
   * Internal constructor to initialize instance.
   * 
   * @param dirty True if the page is created or cloned. False if read or
   *        committed.
   * @param referenceCount Number of references of page.
   */
  private Page(final boolean dirty, final int referenceCount) {
    mReferences = new PageReference[referenceCount];
    mDirty = dirty;
  }

  /**
   * Create constructor.
   * 
   * @param referenceCount Number of references of page.
   */
  protected Page(final int referenceCount) {
    this(true, referenceCount);
  }

  /**
   * Read constructor.
   * 
   * @param referenceCount Number of references of page.
   * @param in Input reader to read from.
   */
  protected Page(final int referenceCount, final FastByteArrayReader in) {
    this(false, referenceCount);
    for (int offset = 0; offset < referenceCount; offset++) {
      if (in.readBoolean()) {
        mReferences[offset] = new PageReference(in);
      }
    }
  }

  /**
   * Clone constructor used for COW.
   * 
   * @param referenceCount Number of references of page.
   * @param committedPage Page to clone.
   */
  protected Page(
      final int referenceCount,
      final Page committedPage) {
    this(true, referenceCount);

    for (int offset = 0; offset < referenceCount; offset++) {
      if (committedPage.mReferences[offset] != null) {
        mReferences[offset] =
            new PageReference(committedPage.mReferences[offset]);
      }
    }
  }

  /**
   * Is this page dirty?
   * 
   * @return True if the page was created or cloned. False if it was read.
   */
  public final boolean isDirty() {
    return mDirty;
  }

  /**
   * Get page reference of given offset.
   * 
   * @param offset Offset of page reference.
   * @return PageReference at given offset.
   */
  public final PageReference getReference(final int offset) {
    if (mReferences[offset] == null) {
      mReferences[offset] = new PageReference();
    }
    return mReferences[offset];
  }

  /**
   * Set page reference at given offset.
   * 
   * @param offset Offset of page reference.
   * @param reference Page reference to set.
   */
  public final void setReference(final int offset, final PageReference reference) {
    mReferences[offset] = reference;
  }

  /**
   * Recursively call commit on all referenced pages.
   * 
   * @param state IWriteTransaction state.
   * @throws IOException occurring during commit operation.
   */
  public void commit(final WriteTransactionState state) throws IOException {
    for (final PageReference reference : mReferences) {
      state.commit(reference);
    }
    mDirty = false;
  }

  /**
   * Serialize page references into output.
   * 
   * @param out Output stream.
   */
  public void serialize(final FastByteArrayWriter out) {
    for (final PageReference reference : mReferences) {
      if (reference != null) {
        out.writeBoolean(true);
        reference.serialize(out);
      } else {
        out.writeBoolean(false);
      }
    }
  }

}
