/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

/**
 * <h1>Page</h1>
 * 
 * <p>
 * Class to provide basic reference handling functionality.
 * </p>
 */
public abstract class AbsPage {

    /** Page references. */
    private final PageReference[] mReferences;

    /** revision of this page. */
    private final long mRevision;

    /**
     * Constructor to initialize instance.
     * 
     * @param referenceCount
     *            Number of references of page.
     * @param revision
     *            Revision Number.
     */
    protected AbsPage(final int referenceCount, final long revision) {

        mReferences = new PageReference[referenceCount];
        mRevision = revision;
    }

    /**
     * Read constructor.
     * 
     * @param referenceCount
     *            Number of references of page.
     * @param mIn
     *            Input reader to read from.
     */
    protected AbsPage(final int referenceCount, final ITTSource mIn) {
        this(referenceCount, mIn.readLong());
        final int[] values = new int[referenceCount];
        for (int i = 0; i < values.length; i++) {
            values[i] = mIn.readInt();
        }
        for (int offset = 0; offset < referenceCount; offset++) {
            if (values[offset] == 1) {
                getReferences()[offset] = new PageReference(mIn);
            }
        }
    }

    /**
     * Clone constructor used for COW.
     * 
     * @param referenceCount
     *            Number of references of page.
     * @param mCommittedPage
     *            Page to clone.
     * @param revision
     *            Number of Revision.
     */
    protected AbsPage(final int referenceCount, final AbsPage mCommittedPage, final long revision) {

        this(referenceCount, revision);
        for (int offset = 0; offset < referenceCount; offset++) {
            if (mCommittedPage.getReferences()[offset] != null) {
                final PageReference ref = mCommittedPage.getReferences()[offset];
                getReferences()[offset] = new PageReference(ref);
            }
        }
    }

    /**
     * Get page reference of given offset.
     * 
     * @param mOffset
     *            Offset of page reference.
     * @return PageReference at given offset.
     */
    public final PageReference getReference(final int mOffset) {
        if (getReferences()[mOffset] == null) {
            getReferences()[mOffset] = new PageReference();
        }
        return getReferences()[mOffset];
    }

    /**
     * Set page reference at given offset.
     * 
     * @param mOffset
     *            Offset of page reference.
     * @param mReference
     *            Page reference to set.
     */
    public final void setReference(final int mOffset, final PageReference mReference) {
        getReferences()[mOffset] = mReference;
    }

    /**
     * Recursively call commit on all referenced pages.
     * 
     * @param mState
     *            IWriteTransaction state.
     * @throws AbsTTException
     *             thorw when write error
     */

    public final void commit(final WriteTransactionState mState) throws AbsTTException {
        for (final PageReference reference : getReferences()) {
            mState.commit(reference);
        }
    }

    /**
     * Serialize page references into output.
     * 
     * @param mOut
     *            Output stream.
     */
    protected void serialize(final ITTSink mOut) {
        mOut.writeLong(mRevision);
        for (int i = 0; i < getReferences().length; i++) {
            if (getReferences()[i] != null) {
                mOut.writeInt(1);
            } else {
                mOut.writeInt(0);
            }
        }

        for (final PageReference reference : getReferences()) {
            if (reference != null) {
                reference.serialize(mOut);
            }
        }
    }

    /**
     * @return the mReferences
     */
    public PageReference[] getReferences() {
        return mReferences;
    }

    /**
     * @return the mRevision
     */
    public final long getRevision() {
        return mRevision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (getReferences().length > 0) {
            builder.append("References: ");
            for (final PageReference ref : getReferences()) {
                if (ref != null) {
                    builder.append(ref.getKey().getIdentifier()).append(",");
                }
            }
        } else {
            builder.append("No references");
        }
        return builder.toString();
    }

}
