/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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

import java.nio.ByteBuffer;

import org.treetank.api.IReadTransaction;
import org.treetank.nodelayer.DocumentRootNode;
import org.treetank.sessionlayer.WriteTransactionState;
import org.treetank.utils.IConstants;

/**
 * <h1>UberPage</h1>
 * 
 * <p>
 * Uber page holds a reference to the static revision root page tree.
 * </p>
 */
public final class UberPage extends AbstractPage {

  /** Offset of indirect page reference. */
  private static final int INDIRECT_REFERENCE_OFFSET = 0;

  /** Number of revisions. */
  private long mRevisionCount;

  /** True if this uber page is the uber page of a fresh TreeTank file. */
  private boolean mBootstrap;

  /**
   * Create uber page.
   */
  public UberPage() {
    super(1);
    mRevisionCount = IConstants.UBP_ROOT_REVISION_COUNT;
    mBootstrap = true;

    // --- Create revision tree ------------------------------------------------

    // Initialize revision tree to guarantee that there is a revision root page.
    IndirectPage page = null;
    PageReference reference = getReference(INDIRECT_REFERENCE_OFFSET);

    // Remaining levels.
    for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
      page = new IndirectPage();
      reference.setPage(page);
      reference = page.getReference(0);
    }

    RevisionRootPage rrp = new RevisionRootPage();
    reference.setPage(rrp);

    // --- Create node tree ----------------------------------------------------

    // Initialize revision tree to guarantee that there is a revision root page.
    page = null;
    reference = rrp.getIndirectPageReference();

    // Remaining levels.
    for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
      page = new IndirectPage();
      reference.setPage(page);
      reference = page.getReference(0);
    }

    NodePage ndp = new NodePage(IConstants.ROOT_PAGE_KEY);
    reference.setPage(ndp);

    ndp.setNode(
        (int) IReadTransaction.DOCUMENT_ROOT_KEY,
        new DocumentRootNode());

    rrp.incrementNodeCountAndMaxNodeKey();
    rrp.incrementNodeCountAndMaxNodeKey();

  }

  /**
   * Read uber page.
   * 
   * @param in Input bytes.
   */
  public UberPage(final ByteBuffer in) {
    super(1, in);
    mRevisionCount = in.getLong();
    mBootstrap = false;
  }

  /**
   * Clone uber page.
   * 
   * @param committedUberPage Page to clone.
   */
  public UberPage(final UberPage committedUberPage) {
    super(1, committedUberPage);
    if (committedUberPage.isBootstrap()) {
      mRevisionCount = committedUberPage.mRevisionCount;
      mBootstrap = committedUberPage.mBootstrap;
    } else {
      mRevisionCount = committedUberPage.mRevisionCount + 1;
      mBootstrap = false;
    }

  }

  /**
   * Get indirect page reference.
   * 
   * @return Indirect page reference.
   */
  public final PageReference<IndirectPage> getIndirectPageReference() {
    return getReference(INDIRECT_REFERENCE_OFFSET);
  }

  /**
   * Get number of revisions.
   * 
   * @return Number of revisions.
   */
  public final long getRevisionCount() {
    return mRevisionCount;
  }

  /**
   * Get key of last committed revision.
   * 
   * @return Key of last committed revision.
   */
  public final long getLastCommittedRevisionNumber() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_NUMBER;
    } else {
      return mRevisionCount - 2;
    }
  }

  /**
   * Get revision key of current in-memory state.
   * 
   * @return Revision key.
   */
  public final long getRevisionNumber() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_NUMBER;
    } else {
      return mRevisionCount - 1;
    }
  }

  /**
   * Flag to indicate whether this uber page is the first ever.
   * 
   * @return True if this uber page is the first one of the TreeTank file.
   */
  public final boolean isBootstrap() {
    return mBootstrap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void commit(final WriteTransactionState state) {
    super.commit(state);
    mBootstrap = false;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final ByteBuffer out) {
    super.serialize(out);
    out.putLong(mRevisionCount);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString()
        + ": revisionCount="
        + mRevisionCount
        + ", indirectPage=("
        + getReference(INDIRECT_REFERENCE_OFFSET)
        + "), isDirty="
        + isDirty()
        + ", isBootstrap="
        + mBootstrap;
  }

}
