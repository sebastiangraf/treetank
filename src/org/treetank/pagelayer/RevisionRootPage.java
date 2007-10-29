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
public final class RevisionRootPage extends AbstractPage {

  /** Offset of name page reference. */
  private static final int NAME_REFERENCE_OFFSET = 0;

  /** Offset of indirect page reference. */
  private static final int INDIRECT_REFERENCE_OFFSET = 1;

  /** Key of revision. */
  private final long mRevisionNumber;

  /** Number of nodes of this revision. */
  private long mRevisionSize;

  /** Last allocated node key. */
  private long mMaxNodeKey;

  /** Timestamp of revision. */
  private long mRevisionTimestamp;

  /**
   * Create revision root page.
   */
  public RevisionRootPage() {
    super(2);
    mRevisionNumber = IConstants.UBP_ROOT_REVISION_KEY;
    mRevisionSize = 0L;
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
    mRevisionNumber = revisionKey;
    mRevisionSize = in.readVarLong();
    mMaxNodeKey = in.readVarLong();
    mRevisionTimestamp = in.readVarLong();
  }

  /**
   * Clone revision root page.
   * 
   * @param committedRevisionRootPage Page to clone.
   */
  public RevisionRootPage(final RevisionRootPage committedRevisionRootPage) {
    super(2, committedRevisionRootPage);
    mRevisionNumber = committedRevisionRootPage.mRevisionNumber + 1;
    mRevisionSize = committedRevisionRootPage.mRevisionSize;
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
   * Get number of revision.
   * 
   * @return Revision number.
   */
  public final long getRevisionNumber() {
    return mRevisionNumber;
  }

  /**
   * Get size of revision, i.e., the node count visible in this revision.
   * 
   * @return Revision size.
   */
  public final long getRevisionSize() {
    return mRevisionSize;
  }

  /**
   * Get timestamp of revision.
   * 
   * @return Revision timestamp.
   */
  public final long getRevisionTimestamp() {
    return mRevisionTimestamp;
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
    mRevisionSize -= 1;
  }

  /**
   * Increment number of nodes by one while allocating another key.
   */
  public final void incrementNodeCountAndMaxNodeKey() {
    mRevisionSize += 1;
    mMaxNodeKey += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void commit(final WriteTransactionState state)
      throws IOException {
    super.commit(state);
    mRevisionTimestamp = System.currentTimeMillis();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    super.serialize(out);
    out.writeVarLong(mRevisionSize);
    out.writeVarLong(mMaxNodeKey);
    out.writeVarLong(mRevisionTimestamp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString()
        + ": revisionKey="
        + mRevisionNumber
        + ", revisionSize="
        + mRevisionSize
        + ", revisionTimestamp="
        + mRevisionTimestamp
        + ", namePage=("
        + getReference(NAME_REFERENCE_OFFSET)
        + "), indirectPage=("
        + getReference(INDIRECT_REFERENCE_OFFSET)
        + "), isDirty="
        + isDirty();
  }

}
