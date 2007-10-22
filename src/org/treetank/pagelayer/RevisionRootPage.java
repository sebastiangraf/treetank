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

final public class RevisionRootPage implements IPage {

  /** True if page was created or cloned. False if it was read or committed. */
  private boolean mDirty;

  private final long mRevisionKey;

  private long mNodeCount;

  /** Map the hash of a name to its name. */
  private PageReference mNamePageReference;

  private long mMaxNodeKey;

  private PageReference mIndirectPageReference;

  private RevisionRootPage(final boolean dirty, final long revisionKey) {
    mDirty = dirty;
    mRevisionKey = revisionKey;
    mNamePageReference = null;
    mIndirectPageReference = null;
  }

  /**
   * This is only required to bootstrap an empty TreeTank.
   * 
   * @return Bootstrapped revision root page.
   */
  public static final RevisionRootPage create() {

    final RevisionRootPage revisionRootPage =
        new RevisionRootPage(true, IConstants.UBP_ROOT_REVISION_KEY);

    // Revisioning (deep init).
    revisionRootPage.mNodeCount = 0L;

    // Name page (shallow init).
    revisionRootPage.mNamePageReference = new PageReference();
    revisionRootPage.mNamePageReference.setPage(NamePage.create());

    // Node pages (shallow init).
    revisionRootPage.mMaxNodeKey = -1L;

    // Indirect pages (shallow init).
    revisionRootPage.mIndirectPageReference = new PageReference();

    return revisionRootPage;

  }

  public static final RevisionRootPage read(
      final FastByteArrayReader in,
      final long revisionKey) {

    final RevisionRootPage revisionRootPage =
        new RevisionRootPage(false, revisionKey);

    // Revisioning (deep load).
    revisionRootPage.mNodeCount = in.readVarLong();

    // Name page (shallow load without name page instance).
    revisionRootPage.mNamePageReference = new PageReference(in);

    // Node pages (shallow load without node page instances).
    revisionRootPage.mMaxNodeKey = in.readVarLong();

    // Indirect node pages (shallow load without indirect page instances).
    revisionRootPage.mIndirectPageReference = new PageReference(in);

    return revisionRootPage;

  }

  public static final RevisionRootPage clone(
      final RevisionRootPage committedRevisionRootPage) {

    final RevisionRootPage revisionRootPage =
        new RevisionRootPage(true, committedRevisionRootPage.mRevisionKey + 1);

    // Revisioning (deep COW).
    revisionRootPage.mNodeCount = committedRevisionRootPage.mNodeCount;

    // Names (deep COW).
    revisionRootPage.mNamePageReference =
        new PageReference(committedRevisionRootPage.mNamePageReference);

    // INode pages (shallow COW without node page instances).
    revisionRootPage.mMaxNodeKey = committedRevisionRootPage.mMaxNodeKey;

    // Indirect node pages (shallow COW without node page instances).
    revisionRootPage.mIndirectPageReference =
        new PageReference(committedRevisionRootPage.mIndirectPageReference);

    return revisionRootPage;
  }

  public final PageReference getNamePageReference() {
    return mNamePageReference;
  }

  public final PageReference getIndirectPageReference() {
    return mIndirectPageReference;
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

  public final long getMaxNodeKey() {
    return mMaxNodeKey;
  }

  public final void decrementNodeCount() {
    mNodeCount -= 1;
  }

  public final void incrementNodeCountAndMaxNodeKey() {
    mNodeCount += 1;
    mMaxNodeKey += 1;
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final IWriteTransactionState state) throws Exception {
    state.commit(mNamePageReference);
    state.commit(mIndirectPageReference);
    mDirty = false;
  }

  /**
   * {@inheritDoc}
   */
  public void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(mNodeCount);
    mNamePageReference.serialize(out);
    out.writeVarLong(mMaxNodeKey);
    mIndirectPageReference.serialize(out);
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
    return super.toString()
        + ": revisionKey="
        + mRevisionKey
        + ", nodeCount="
        + mNodeCount
        + ", namePage=("
        + mNamePageReference
        + "), indirectPage=("
        + mIndirectPageReference
        + "), isDirty="
        + mDirty;
  }

}
