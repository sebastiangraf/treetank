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

import java.util.Arrays;

import org.treetank.access.PageWriteTrx;
import org.treetank.exception.TTException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>IndirectPage</h1>
 * 
 * <p>
 * Indirect page holds a set of references to build a reference tree.
 * </p>
 */
public final class IndirectPage implements IPage {

    /** Page references. */
    private PageReference[] mReferences;

    /** revision of this page. */
    private final long mRevision;

    /**
     * Create indirect page.
     * 
     * @param paramRevision
     *            Revision Number
     */
    public IndirectPage(final long paramRevision) {
        mRevision = paramRevision;
        mReferences = new PageReference[IConstants.INP_REFERENCE_COUNT];
        for (int i = 0; i < mReferences.length; i++) {
            mReferences[i] = new PageReference();
        }
    }

    /**
     * Clone indirect page.
     * 
     * @param page
     *            Page to clone.
     * @param revisionToUse
     *            Revision number to use
     */
    public IndirectPage(final IndirectPage page, final long revisionToUse) {
        mRevision = revisionToUse;
        mReferences = page.getReferences();
    }

    @Override
    public void commit(PageWriteTrx paramState) throws TTException {
        for (final PageReference reference : getReferences()) {
            paramState.commit(reference);
        }
    }

    @Override
    public PageReference[] getReferences() {
        return mReferences;
    }

    @Override
    public long getRevision() {
        return mRevision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.INDIRCTPAGE);
        pOutput.writeLong(mRevision);
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
        builder.append("IndirectPage [mReferences=");
        builder.append(Arrays.toString(mReferences));
        builder.append(", mRevision=");
        builder.append(mRevision);
        builder.append("]");
        return builder.toString();
    }

}
