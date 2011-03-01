/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.page;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * <h1>RevisionRootPage</h1>
 * 
 * <p>
 * Revision root page holds a reference to the name page as well as the static node page tree.
 * </p>
 */
public final class RevisionRootPage extends AbsPage {

    /** Offset of name page reference. */
    private static final int NAME_REFERENCE_OFFSET = 0;

    /** Offset of indirect page reference. */
    private static final int INDIRECT_REFERENCE_OFFSET = 1;

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
        super(2, IConstants.UBP_ROOT_REVISION_NUMBER);
        mRevisionSize = 0L;
        final PageReference ref = getReference(NAME_REFERENCE_OFFSET);
        ref.setPage(new NamePage(IConstants.UBP_ROOT_REVISION_NUMBER));
        mMaxNodeKey = -1L;
    }

    /**
     * Read revision root page.
     * 
     * @param mIn
     *            Input bytes.
     */
    protected RevisionRootPage(final ITTSource mIn) {
        super(2, mIn);
        mRevisionSize = mIn.readLong();
        mMaxNodeKey = mIn.readLong();
        mRevisionTimestamp = mIn.readLong();
    }

    /**
     * Clone revision root page.
     * 
     * @param mCommittedRevisionRootPage
     *            Page to clone.
     * @param revisionToUse
     *            Revision number to use.
     */
    public RevisionRootPage(final RevisionRootPage mCommittedRevisionRootPage, final long revisionToUse) {
        super(2, mCommittedRevisionRootPage, revisionToUse);
        mRevisionSize = mCommittedRevisionRootPage.mRevisionSize;
        mMaxNodeKey = mCommittedRevisionRootPage.mMaxNodeKey;
    }

    /**
     * Get name page reference.
     * 
     * @return Name page reference.
     */
    public PageReference getNamePageReference() {
        return getReference(NAME_REFERENCE_OFFSET);
    }

    /**
     * Get indirect page reference.
     * 
     * @return Indirect page reference.
     */
    public PageReference getIndirectPageReference() {
        return getReference(INDIRECT_REFERENCE_OFFSET);
    }

    /**
     * Get size of revision, i.e., the node count visible in this revision.
     * 
     * @return Revision size.
     */
    public long getRevisionSize() {
        return mRevisionSize;
    }

    /**
     * Get timestamp of revision.
     * 
     * @return Revision timestamp.
     */
    public long getRevisionTimestamp() {
        return mRevisionTimestamp;
    }

    /**
     * Get last allocated node key.
     * 
     * @return Last allocated node key.
     */
    public long getMaxNodeKey() {
        return mMaxNodeKey;
    }

    /**
     * Increment number of nodes by one while allocating another key.
     */
    public void incrementMaxNodeKey() {
        mMaxNodeKey += 1;
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public final void commit(final WriteTransactionState state) {
    // super.commit(state);
    // mRevisionTimestamp = System.currentTimeMillis();
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serialize(final ITTSink mOut) {
        mRevisionTimestamp = System.currentTimeMillis();
        super.serialize(mOut);
        mOut.writeLong(mRevisionSize);
        mOut.writeLong(mMaxNodeKey);
        mOut.writeLong(mRevisionTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " revisionSize=" + mRevisionSize + ", revisionTimestamp="
            + mRevisionTimestamp + ", namePage=(" + getReference(NAME_REFERENCE_OFFSET) + "), indirectPage=("
            + getReference(INDIRECT_REFERENCE_OFFSET) + ")";
    }

}
