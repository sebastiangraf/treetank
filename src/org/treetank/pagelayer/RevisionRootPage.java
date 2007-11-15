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
    mRevisionNumber = IConstants.UBP_ROOT_REVISION_NUMBER;
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
  public final PageReference<NamePage> getNamePageReference() {
    return getReference(NAME_REFERENCE_OFFSET);
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
  public final void commit(final WriteTransactionState state) {
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
