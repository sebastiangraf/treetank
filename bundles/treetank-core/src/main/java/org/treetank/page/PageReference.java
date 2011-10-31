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

import org.treetank.io.IKey;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.io.KeyDelegate;
import org.treetank.io.KeyPersistenter;

/**
 * <h1>PageReference</h1>
 * 
 * <p>
 * Page reference pointing to a page. This might be on stable storage pointing
 * to the start byte in a file, including the length in bytes, and the checksum
 * of the serialized page. Or it might be an immediate reference to an in-memory
 * instance of the deserialized page.
 * </p>
 * 
 * 
 */
@Deprecated
public final class PageReference {

    /** In-memory deserialized page instance. */
    private IPage mPage;

    /** Corresponding mKey of the related node page. */
    private long mNodePageKey = -1;

    /** Key in persistent storage. */
    private IKey mKey;

    /**
     * Default constructor setting up an uninitialized page reference.
     */
    public PageReference() {
        this(null, null);
    }

    /**
     * Constructor to clone an existing page reference.
     * 
     * @param paramPageReference
     *            Page reference to clone.
     */
    public PageReference(final PageReference paramPageReference) {
        this(paramPageReference.mPage, paramPageReference.mKey);
    }

    /**
     * Constructor to properly set up a page reference.
     * 
     * @param paramPage
     *            In-memory deserialized page instance.
     * @param paramKey
     *            {@link KeyDelegate} of the page to be referenced in the
     *            persistent storage
     */
    public PageReference(final IPage paramPage, final IKey paramKey) {
        mPage = paramPage;
        mKey = paramKey;
    }

    /**
     * Read page reference from storage.
     * 
     * @param paramIn
     *            Input bytes.
     */
    public PageReference(final ITTSource paramIn) {
        mPage = null;
        mKey = KeyPersistenter.createKey(paramIn);
    }

    /**
     * Is there an instantiated page?
     * 
     * @return True if the reference points to an in-memory instance.
     */
    public boolean isInstantiated() {
        return mPage != null;
    }

    /**
     * Was the referenced page ever committed?
     * 
     * @return True if the page was committed.
     */
    public boolean isCommitted() {
        return mKey != null;
    }

    /**
     * Get in-memory instance of deserialized page.
     * 
     * @return In-memory instance of deserialized page.
     */
    public IPage getPage() {
        return mPage;
    }

    /**
     * Set in-memory instance of deserialized page.
     * 
     * @param paramPage
     *            Deserialized page.
     */
    public void setPage(final IPage paramPage) {
        mPage = paramPage;
    }

    /**
     * Get start byte offset in file.
     * 
     * @return Start offset in file.
     */
    public IKey getKey() {
        return mKey;
    }

    /**
     * Set start byte offset in file.
     * 
     * @param paramKey
     *            Key of this reference set by the persistent storage
     */
    public void setKey(final IKey paramKey) {
        mKey = paramKey;
    }

    /**
     * Serialize page reference to output.
     * 
     * @param mOut
     *            Output bytes that get written to a file.
     */
    public void serialize(final ITTSink mOut) {
        KeyPersistenter.serializeKey(mOut, mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString());
        if (this.mKey != null) {
            builder.append(": key=");
            builder.append(mKey.toString());
        } else {
            builder.append(": key=null");
        }
        builder.append(", page=(");
        builder.append(mPage);
        builder.append(")");
        return builder.toString();
    }

    /**
     * Set nodepage key.
     * 
     * @param paramNodePageKey
     *            the nodePageKey to set
     */
    public void setNodePageKey(final long paramNodePageKey) {
        mNodePageKey = paramNodePageKey;
    }

    /**
     * Get nodepage key.
     * 
     * @return the nodePageKey
     */
    public long getNodePageKey() {
        return mNodePageKey;
    }

}
