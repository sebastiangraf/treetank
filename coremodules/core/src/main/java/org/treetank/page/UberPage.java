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

import org.treetank.access.PageWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.page.interfaces.IReferencePage;
import org.treetank.page.interfaces.IRevisionPage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>UberPage</h1>
 * 
 * <p>
 * Uber page holds a reference to the static revision root page tree.
 * </p>
 */
public final class UberPage implements IRevisionPage, IReferencePage {

    /** Number of revisions. */
    private final long mRevisionCount;

    /** True if this uber page is the uber page of a fresh TreeTank file. */
    private boolean mBootstrap;

    /** Revision of this page. */
    private final long mRevision;

    /** Page references. */
    private PageReference mReference;


    /**
     * Clone uber page.
     * 
     * @param paramCommittedUberPage
     *            Page to clone.
     * @param pRevToUse
     *            Revision number to use.
     */
    public UberPage(final long pRevision, final long pRevisionCount) {
        mRevision = pRevision;
        mRevisionCount = pRevisionCount;
        mReference = new PageReference();

    }

    /**
     * Clone uber page.
     * 
     * @param paramCommittedUberPage
     *            Page to clone.
     * @param pRevToUse
     *            Revision number to use.
     */
    public UberPage(final UberPage paramCommittedUberPage, final long pRevToUse) {
        mRevision = pRevToUse;
        mReference = paramCommittedUberPage.getReferences()[0];
        if (paramCommittedUberPage.isBootstrap()) {
            mRevisionCount = paramCommittedUberPage.mRevisionCount;
            mBootstrap = paramCommittedUberPage.mBootstrap;
        } else {
            mRevisionCount = paramCommittedUberPage.mRevisionCount + 1;
            mBootstrap = false;
        }
    }

    /**
     * Get indirect page reference.
     * 
     * @return Indirect page reference.
     */
    public PageReference getIndirectPageReference() {
        return getReferences()[0];
    }

    /**
     * Get number of revisions.
     * 
     * @return Number of revisions.
     */
    public long getRevisionCount() {
        return mRevisionCount;
    }

    /**
     * Get key of last committed revision.
     * 
     * @return Key of last committed revision.
     */
    public long getLastCommittedRevisionNumber() {
        return mRevisionCount - 2;
    }

    /**
     * Get revision key of current in-memory state.
     * 
     * @return Revision key.
     */
    public long getRevisionNumber() {
        return mRevisionCount - 1;
    }

    /**
     * Flag to indicate whether this uber page is the first ever.
     * 
     * @return True if this uber page is the first one of the TreeTank file.
     */
    public boolean isBootstrap() {
        return mBootstrap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        mBootstrap = false;
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.UBERPAGE);
        pOutput.writeLong(mRevision);
        pOutput.writeLong(mRevisionCount);
        for (final PageReference reference : getReferences()) {
            pOutput.writeLong(reference.getKey());
        }
        return pOutput.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UberPage [mRevisionCount=");
        builder.append(mRevisionCount);
        builder.append(", mBootstrap=");
        builder.append(mBootstrap);
        builder.append(", mRevision=");
        builder.append(mRevision);
        builder.append(", mReference=");
        builder.append(mReference.toString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void commit(PageWriteTrx paramState) throws TTException {
        for (final PageReference reference : getReferences()) {
            paramState.commit(reference);
        }
    }

    @Override
    public PageReference[] getReferences() {
        return new PageReference[] {
            mReference
        };
    }

    @Override
    public long getRevision() {
        return mRevision;
    }

}
