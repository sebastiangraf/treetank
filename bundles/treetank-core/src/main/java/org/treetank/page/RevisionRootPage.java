/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.page;

import org.treetank.access.WriteTransactionState;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.utils.IConstants;

/**
 * <h1>RevisionRootPage</h1>
 * 
 * <p>
 * Revision root page holds a reference to the name page as well as the static node page tree.
 * </p>
 */
public final class RevisionRootPage implements IPage {

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

    private final AbsPage mDelegate;

    /**
     * Create revision root page.
     */
    public RevisionRootPage() {
        mDelegate = new AbsPage(2, IConstants.UBP_ROOT_REVISION_NUMBER);
        mRevisionSize = 0L;
        final PageReference ref = getChildren(NAME_REFERENCE_OFFSET);
        ref.setPage(new NamePage(IConstants.UBP_ROOT_REVISION_NUMBER));
        mMaxNodeKey = -1L;
    }

    /**
     * Read revision root page.
     * 
     * @param paramIn
     *            Input bytes.
     */
    protected RevisionRootPage(final ITTSource paramIn) {
        mDelegate = new AbsPage(2, paramIn.readLong());
        mDelegate.initialize(2, paramIn);
        mRevisionSize = paramIn.readLong();
        mMaxNodeKey = paramIn.readLong();
        mRevisionTimestamp = paramIn.readLong();
    }

    /**
     * Clone revision root page.
     * 
     * @param paramCommittedRevisionRootPage
     *            Page to clone.
     * @param paramRevisionToUse
     *            Revision number to use.
     */
    public RevisionRootPage(final RevisionRootPage paramCommittedRevisionRootPage,
        final long paramRevisionToUse) {
        mDelegate = new AbsPage(2, paramRevisionToUse);
        mDelegate.initialize(2, paramCommittedRevisionRootPage);
        mRevisionSize = paramCommittedRevisionRootPage.mRevisionSize;
        mMaxNodeKey = paramCommittedRevisionRootPage.mMaxNodeKey;
    }

    /**
     * Get name page reference.
     * 
     * @return Name page reference.
     */
    public PageReference getNamePageReference() {
        return getChildren(NAME_REFERENCE_OFFSET);
    }

    /**
     * Get indirect page reference.
     * 
     * @return Indirect page reference.
     */
    public PageReference getIndirectPageReference() {
        return getChildren(INDIRECT_REFERENCE_OFFSET);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mOut) {
        mRevisionTimestamp = System.currentTimeMillis();
        mDelegate.serialize(mOut);
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
            + mRevisionTimestamp + ", namePage=(" + getChildren(NAME_REFERENCE_OFFSET) + "), indirectPage=("
            + getChildren(INDIRECT_REFERENCE_OFFSET) + ")";
    }

    @Override
    public PageReference getChildren(int paramOffset) {
        return mDelegate.getChildren(paramOffset);
    }

    @Override
    public void commit(WriteTransactionState paramState) throws AbsTTException {
        mDelegate.commit(paramState);
    }

    @Override
    public PageReference[] getReferences() {
        return mDelegate.getReferences();
    }

    @Override
    public long getRevision() {
        return mDelegate.getRevision();
    }

}
