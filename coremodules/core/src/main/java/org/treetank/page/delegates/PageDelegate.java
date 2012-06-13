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

package org.treetank.page.delegates;

import org.treetank.access.PageWriteTrx;
import org.treetank.exception.AbsTTException;
import org.treetank.io.EStorage;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.page.IPage;
import org.treetank.page.PageReference;

/**
 * <h1>Page</h1>
 * 
 * <p>
 * Class to provide basic reference handling functionality.
 * </p>
 */
public class PageDelegate implements IPage {

    /** Page references. */
    private PageReference[] mReferences;

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
    public PageDelegate(final int paramReferenceCount, final long paramRevision) {
        mReferences = new PageReference[paramReferenceCount];
        mRevision = paramRevision;
        for (int i = 0; i < paramReferenceCount; i++) {
            mReferences[i] = new PageReference();
        }
    }

    public void initialize(final ITTSource paramIn) {
        for (int offset = 0; offset < mReferences.length; offset++) {
            getReferences()[offset] = new PageReference();
            final EStorage storage = EStorage.getInstance(paramIn.readInt());
            if (storage != null) {
                getReferences()[offset].setKey(storage.deserialize(paramIn));
            }
        }
    }

    public void initialize(final IPage paramCommittedPage) {
        mReferences = paramCommittedPage.getReferences();
    }

    /**
     * Get page reference of given offset.
     * 
     * @param paramOffset
     *            Offset of page reference.
     * @return PageReference at given offset.
     */
    public final PageReference getChildren(final int paramOffset) {
        if (getReferences()[paramOffset] == null) {
            getReferences()[paramOffset] = new PageReference();
        }
        return getReferences()[paramOffset];
    }

    /**
     * Recursively call commit on all referenced pages.
     * 
     * @param paramState
     *            INodeWriteTrx state.
     * @throws AbsTTException
     *             thorw when write error
     */

    public final void commit(final PageWriteTrx paramState) throws AbsTTException {
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
    public void serialize(final ITTSink paramOut) {
        paramOut.writeLong(mRevision);

        for (final PageReference reference : getReferences()) {
            if (reference.getKey() == null) {
                paramOut.writeInt(0);
            } else {
                EStorage.getInstance(reference.getKey().getClass()).serialize(paramOut, reference.getKey());
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

}
