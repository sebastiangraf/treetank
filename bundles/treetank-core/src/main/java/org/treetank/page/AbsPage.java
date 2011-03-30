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
     * @param paramReferenceCount
     *            Number of references of page.
     * @param paramRevision
     *            Revision Number.
     */
    protected AbsPage(final int paramReferenceCount, final long paramRevision) {
        mReferences = new PageReference[paramReferenceCount];
        mRevision = paramRevision;
    }

    /**
     * Read constructor.
     * 
     * @param paramReferenceCount
     *            Number of references of page.
     * @param paramIn
     *            Input reader to read from.
     */
    protected AbsPage(final int paramReferenceCount, final ITTSource paramIn) {
        this(paramReferenceCount, paramIn.readLong());
        final int[] values = new int[paramReferenceCount];
        for (int i = 0; i < values.length; i++) {
            values[i] = paramIn.readInt();
        }
        for (int offset = 0; offset < paramReferenceCount; offset++) {
            if (values[offset] == 1) {
                getReferences()[offset] = new PageReference(paramIn);
            }
        }
    }

    /**
     * Clone constructor used for COW.
     * 
     * @param paramReferenceCount
     *            Number of references of page.
     * @param paramCommittedPage
     *            Page to clone.
     * @param paramRevision
     *            Number of Revision.
     */
    protected AbsPage(final int paramReferenceCount, final AbsPage paramCommittedPage,
        final long paramRevision) {
        this(paramReferenceCount, paramRevision);
        for (int offset = 0; offset < paramReferenceCount; offset++) {
            if (paramCommittedPage.getReferences()[offset] != null) {
                final PageReference ref = paramCommittedPage.getReferences()[offset];
                getReferences()[offset] = new PageReference(ref);
            }
        }
    }

    /**
     * Get page reference of given offset.
     * 
     * @param paramOffset
     *            Offset of page reference.
     * @return PageReference at given offset.
     */
    public final PageReference getReference(final int paramOffset) {
        if (getReferences()[paramOffset] == null) {
            getReferences()[paramOffset] = new PageReference();
        }
        return getReferences()[paramOffset];
    }

    /**
     * Set page reference at given offset.
     * 
     * @param paramOffset
     *            Offset of page reference.
     * @param paramReference
     *            Page reference to set.
     */
    public final void setReference(final int paramOffset, final PageReference paramReference) {
        getReferences()[paramOffset] = paramReference;
    }

    /**
     * Recursively call commit on all referenced pages.
     * 
     * @param paramState
     *            IWriteTransaction state.
     * @throws AbsTTException
     *             thorw when write error
     */

    public final void commit(final WriteTransactionState paramState) throws AbsTTException {
        for (final PageReference reference : getReferences()) {
            paramState.commit(reference);
        }
    }

    /**
     * Serialize page references into output.
     * 
     * @param paramOut
     *            Output stream.
     */
    protected void serialize(final ITTSink paramOut) {
        paramOut.writeLong(mRevision);
        for (int i = 0; i < getReferences().length; i++) {
            if (getReferences()[i] != null) {
                paramOut.writeInt(1);
            } else {
                paramOut.writeInt(0);
            }
        }

        for (final PageReference reference : getReferences()) {
            if (reference != null) {
                reference.serialize(paramOut);
            }
        }
    }

    /**
     * @return the mReferences
     */
    public final PageReference[] getReferences() {
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
