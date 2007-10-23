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
import org.treetank.api.IPage;
import org.treetank.sessionlayer.WriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

final public class UberPage implements IPage {

  /** True if page was created or cloned. False if it was read or committed. */
  private boolean mDirty;

  private long mRevisionCount;

  private PageReference mIndirectPageReference;

  private boolean mBootstrap;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private UberPage(final boolean dirty, final boolean bootstrap) {
    mDirty = dirty;
    mIndirectPageReference = null;
    mBootstrap = bootstrap;
  }

  /**
   * Create new uncommitted in-memory uber page. This is only required
   * to bootstrap an empty TreeTank.
   * 
   * @return Bootstrapped uber page.
   * @throws Exception
   */
  public UberPage() {

    // --- Create uber page ----------------------------------------------------

    this(true, true);

    // Make sure that all references are instantiated.
    mRevisionCount = IConstants.UBP_ROOT_REVISION_COUNT;

    // Indirect pages (shallow init).
    mIndirectPageReference = new PageReference();

    // --- Create revision tree ------------------------------------------------

    // Initialize revision tree to guarantee that there is a revision root page.
    IndirectPage page = null;
    PageReference reference = mIndirectPageReference;

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

    ndp.setNode(0, new Node(IConstants.ROOT_KEY));

    rrp.incrementNodeCountAndMaxNodeKey();

  }

  /**
   * Read committed uber page from disk.
   * 
   * @param pageCache
   * @param in
   * @throws Exception
   */
  public UberPage(final FastByteArrayReader in) {

    this(false, false);

    // Deserialize uber page.
    mRevisionCount = in.readVarLong();

    // Indirect pages (shallow load without indirect page instances).
    mIndirectPageReference = new PageReference(in);
  }

  /**
   * COW committed uber page to modify it.
   * 
   * @param committedUberPage
   * @return
   */
  public UberPage(final UberPage committedUberPage) {

    // Make sure that the uber page is only cloned if it is not the first one.
    if (committedUberPage.mBootstrap) {
      mDirty = committedUberPage.mDirty;
      mIndirectPageReference =
          new PageReference(committedUberPage.mIndirectPageReference);
      mBootstrap = committedUberPage.mBootstrap;
      mRevisionCount = committedUberPage.mRevisionCount;
    } else {
      mDirty = true;
      mIndirectPageReference =
          new PageReference(committedUberPage.mIndirectPageReference);
      mBootstrap = false;
      mRevisionCount = committedUberPage.mRevisionCount + 1;
    }
  }

  public final PageReference getIndirectPageReference() {
    return mIndirectPageReference;
  }

  public final long getRevisionCount() {
    return mRevisionCount;
  }

  public final long getLastCommittedRevisionKey() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_KEY;
    } else {
      return mRevisionCount - 2;
    }
  }

  public final long getRevisionKey() {
    if (mRevisionCount == IConstants.UBP_ROOT_REVISION_COUNT) {
      return IConstants.UBP_ROOT_REVISION_KEY;
    } else {
      return mRevisionCount - 1;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final WriteTransactionState state)
      throws IOException {
    state.commit(mIndirectPageReference);
    mDirty = false;
    mBootstrap = false;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(mRevisionCount);
    mIndirectPageReference.serialize(out);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isDirty() {
    return mDirty;
  }

  public final boolean isBootstrap() {
    return mBootstrap;
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
        + mIndirectPageReference
        + "), isDirty="
        + mDirty
        + ", isBootstrap="
        + mBootstrap;
  }

}
