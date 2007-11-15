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

package org.treetank.pagelayer;

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
public abstract class AbstractPage {

  /** Page references. */
  private final PageReference<? extends AbstractPage>[] mReferences;

  /** True if page was created or cloned. False if it was read or committed. */
  private boolean mDirty;

  /**
   * Internal constructor to initialize instance.
   * 
   * @param dirty True if the page is created or cloned. False if read or
   *        committed.
   * @param referenceCount Number of references of page.
   */
  private AbstractPage(final boolean dirty, final int referenceCount) {
    mReferences = new PageReference[referenceCount];
    mDirty = dirty;
  }

  /**
   * Create constructor.
   * 
   * @param referenceCount Number of references of page.
   */
  protected AbstractPage(final int referenceCount) {
    this(true, referenceCount);
  }

  /**
   * Read constructor.
   * 
   * @param referenceCount Number of references of page.
   * @param in Input reader to read from.
   */
  protected AbstractPage(final int referenceCount, final FastByteArrayReader in) {
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
  protected AbstractPage(
      final int referenceCount,
      final AbstractPage committedPage) {
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
  public final void setReference(
      final int offset,
      final PageReference<? extends AbstractPage> reference) {
    mReferences[offset] = reference;
  }

  /**
   * Recursively call commit on all referenced pages.
   * 
   * @param state IWriteTransaction state.
   */
  public void commit(final WriteTransactionState state) {
    for (final PageReference<? extends AbstractPage> reference : mReferences) {
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
    for (final PageReference<? extends AbstractPage> reference : mReferences) {
      if (reference != null) {
        out.writeBoolean(true);
        reference.serialize(out);
      } else {
        out.writeBoolean(false);
      }
    }
  }

}
