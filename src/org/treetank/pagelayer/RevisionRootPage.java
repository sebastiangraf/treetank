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

final public class RevisionRootPage extends AbstractPage implements IPage {

  private final long mRevisionKey;

  private long mNodeCount;

  /** Map the hash of a name to its name. */
  private PageReference mNamePageReference;

  private long mMaxNodeKey;

  private PageReference mIndirectPageReference;

  private RevisionRootPage(final long revisionKey) {
    mRevisionKey = revisionKey;
    mNamePageReference = null;
    mIndirectPageReference = null;
  }

  public static final RevisionRootPage create(final long revisionKey) {

    final RevisionRootPage revisionRootPage = new RevisionRootPage(revisionKey);

    // Revisioning (deep init).
    revisionRootPage.mNodeCount = 0L;

    // Name page (shallow init).
    revisionRootPage.mNamePageReference = createPageReference();
    revisionRootPage.mNamePageReference.setPage(NamePage.create());

    // Node pages (shallow init).
    revisionRootPage.mMaxNodeKey = -1L;

    // Indirect pages (shallow init).
    revisionRootPage.mIndirectPageReference = createPageReference();

    revisionRootPage.setDirty(true);
    return revisionRootPage;

  }

  public static final RevisionRootPage read(
      final FastByteArrayReader in,
      final long revisionKey) throws Exception {

    final RevisionRootPage revisionRootPage = new RevisionRootPage(revisionKey);

    // Revisioning (deep load).
    revisionRootPage.mNodeCount = in.readVarLong();

    // Name page (shallow load without name page instance).
    revisionRootPage.mNamePageReference = readPageReference(in);

    // Node pages (shallow load without node page instances).
    revisionRootPage.mMaxNodeKey = in.readVarLong();

    // Indirect node pages (shallow load without indirect page instances).
    revisionRootPage.mIndirectPageReference = readPageReference(in);

    revisionRootPage.setDirty(false);
    return revisionRootPage;

  }

  public static final RevisionRootPage clone(
      final RevisionRootPage committedRevisionRootPage) {

    final RevisionRootPage revisionRootPage =
        new RevisionRootPage(committedRevisionRootPage.mRevisionKey + 1);

    // Revisioning (deep COW).
    revisionRootPage.mNodeCount = committedRevisionRootPage.mNodeCount;

    // Names (deep COW).
    revisionRootPage.mNamePageReference =
        clonePageReference(committedRevisionRootPage.mNamePageReference);

    // INode pages (shallow COW without node page instances).
    revisionRootPage.mMaxNodeKey = committedRevisionRootPage.mMaxNodeKey;

    // Indirect node pages (shallow COW without node page instances).
    revisionRootPage.mIndirectPageReference =
        clonePageReference(committedRevisionRootPage.mIndirectPageReference);

    revisionRootPage.setDirty(false);
    return revisionRootPage;
  }

  public final PageReference getNamePageReference() {
    return mNamePageReference;
  }

  public final void setNamePageReference(final PageReference reference) {
    mNamePageReference = reference;
    setDirty(true);
  }

  public final PageReference getIndirectPageReference() {
    return mIndirectPageReference;
  }

  public final void setIndirectPageReference(final PageReference reference) {
    mIndirectPageReference = reference;
    setDirty(true);
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
    setDirty(true);
  }

  public final void incrementNodeCountAndMaxNodeKey() {
    mNodeCount += 1;
    mMaxNodeKey += 1;
    setDirty(true);
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final IWriteTransactionState state) throws Exception {
    state.commit(mNamePageReference);
    state.commit(mIndirectPageReference);
    setDirty(false);
  }

  /**
   * {@inheritDoc}
   */
  public void serialize(final FastByteArrayWriter out) throws Exception {
    out.writeVarLong(mNodeCount);
    serialize(out, mNamePageReference);
    out.writeVarLong(mMaxNodeKey);
    serialize(out, mIndirectPageReference);
  }

}
