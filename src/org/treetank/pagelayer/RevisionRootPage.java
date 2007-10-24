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
import org.treetank.sessionlayer.WriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>RevisionRootPage</h1>
 * 
 * <p>
 * Revision root page holds a reference to the name page as well as
 * the static node page tree.
 * </p>
 */
public final class RevisionRootPage extends Page {

  /** Offset of name page reference. */
  private static final int NAME_REFERENCE_OFFSET = 0;

  /** Offset of indirect page reference. */
  private static final int INDIRECT_REFERENCE_OFFSET = 1;

  /** Key of revision. */
  private final long mRevisionKey;

  /** Number of nodes of this revision. */
  private long mNodeCount;

  /** Last allocated node key. */
  private long mMaxNodeKey;

  /** Timestamp of revision. */
  private long mTimestamp;

  /**
   * Create revision root page.
   */
  public RevisionRootPage() {
    super(2);
    mRevisionKey = IConstants.UBP_ROOT_REVISION_KEY;
    mNodeCount = 0L;
    getReference(NAME_REFERENCE_OFFSET).setPage(new NamePage());
    mMaxNodeKey = -1L;
  }

  /**
   * Read revision root page.
   * 
   * @param in Input bytes.
   * @param revisionKey Key of revision.
   */
  public RevisionRootPage(final FastByteArrayReader in, final long revisionKey) {
    super(2, in);
    mRevisionKey = revisionKey;
    mNodeCount = in.readVarLong();
    mMaxNodeKey = in.readVarLong();
    mTimestamp = in.readVarLong();
  }

  /**
   * Clone revision root page.
   * 
   * @param committedRevisionRootPage Page to clone.
   */
  public RevisionRootPage(final RevisionRootPage committedRevisionRootPage) {
    super(2, committedRevisionRootPage);
    mRevisionKey = committedRevisionRootPage.mRevisionKey + 1;
    mNodeCount = committedRevisionRootPage.mNodeCount;
    mMaxNodeKey = committedRevisionRootPage.mMaxNodeKey;
  }

  /**
   * Get name page reference.
   * 
   * @return Name page reference.
   */
  public final PageReference getNamePageReference() {
    return getReference(NAME_REFERENCE_OFFSET);
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
   * Get key of revision.
   * 
   * @return Revision key.
   */
  public final long getRevisionKey() {
    return mRevisionKey;
  }

  /**
   * Get size of revision, i.e., the node count visible in this revision.
   * 
   * @return Revision size.
   */
  public final long getNodeCount() {
    return mNodeCount;
  }

  /**
   * Get timestamp of revision.
   * 
   * @return Revision timestamp.
   */
  public final long getRevisionTimestamp() {
    return mTimestamp;
  }

  /**
   * Get last allocated node key.
   * 
   * @return Last allocated node key.
   */
  public final long getMaxNodeKey() {
    return mMaxNodeKey;
  }

  /**
   * Decrement number of nodes by one.
   */
  public final void decrementNodeCount() {
    mNodeCount -= 1;
  }

  /**
   * Increment number of nodes by one while allocating another key.
   */
  public final void incrementNodeCountAndMaxNodeKey() {
    mNodeCount += 1;
    mMaxNodeKey += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void commit(final WriteTransactionState state)
      throws IOException {
    super.commit(state);
    mTimestamp = System.currentTimeMillis();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    super.serialize(out);
    out.writeVarLong(mNodeCount);
    out.writeVarLong(mMaxNodeKey);
    out.writeVarLong(mTimestamp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString()
        + ": revisionKey="
        + mRevisionKey
        + ", nodeCount="
        + mNodeCount
        + ", timestamp="
        + mTimestamp
        + ", namePage=("
        + getReference(NAME_REFERENCE_OFFSET)
        + "), indirectPage=("
        + getReference(INDIRECT_REFERENCE_OFFSET)
        + "), isDirty="
        + isDirty();
  }

}
