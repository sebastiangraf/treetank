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
 * <h1>IndirectPage</h1>
 * 
 * <p>
 * Indirect page holds a set of references to build a reference tree.
 * </p>
 */
public final class IndirectPage implements IPage {

    private final PageDelegate mDelegate;

    /**
     * Create indirect page.
     * 
     * @param paramRevision
     *            Revision Number
     */
    public IndirectPage(final long paramRevision) {
        mDelegate = new PageDelegate(IConstants.INP_REFERENCE_COUNT,
                paramRevision);
    }

    /**
     * Read indirect page.
     * 
     * @param paramIn
     *            Input bytes.
     */
    protected IndirectPage(final ITTSource paramIn) {
        mDelegate = new PageDelegate(IConstants.INP_REFERENCE_COUNT,
                paramIn.readLong());
        mDelegate.initialize(paramIn);
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
        mDelegate = new PageDelegate(IConstants.INP_REFERENCE_COUNT,
                revisionToUse);
        mDelegate.initialize(page);
    }

    @Override
    public void commit(WriteTransactionState paramState) throws AbsTTException {
        mDelegate.commit(paramState);
    }

    @Override
    public void serialize(ITTSink paramOut) {
        mDelegate.serialize(paramOut);
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
