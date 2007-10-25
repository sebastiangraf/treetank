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

import java.io.IOException;

import org.treetank.api.IConstants;
import org.treetank.nodelayer.DocumentNode;
import org.treetank.sessionlayer.WriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

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

    ndp.setNode(0, new DocumentNode());

    rrp.incrementNodeCountAndMaxNodeKey();

  }

  /**
   * Read uber page.
   * 
   * @param in Input bytes.
   */
  public UberPage(final FastByteArrayReader in) {
    super(1, in);
    mRevisionCount = in.readVarLong();
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
  public final PageReference getIndirectPageReference() {
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
  public final long getLastCommittedRevisionKey() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_KEY;
    } else {
      return mRevisionCount - 2;
    }
  }

  /**
   * Get revision key of current in-memory state.
   * 
   * @return Revision key.
   */
  public final long getRevisionKey() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_KEY;
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
  public final void commit(final WriteTransactionState state)
      throws IOException {
    super.commit(state);
    mBootstrap = false;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) {
    super.serialize(out);
    out.writeVarLong(mRevisionCount);
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
