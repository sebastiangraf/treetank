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

import com.treetank.io.AbsKey;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.io.KeyPersistenter;
import com.treetank.utils.IConstants;

/**
 * <h1>PageReference</h1>
 * 
 * <p>
 * Page reference pointing to a page. This might be on stable storage pointing to the start byte in a file,
 * including the length in bytes, and the checksum of the serialized page. Or it might be an immediate
 * reference to an in-memory instance of the deserialized page.
 * </p>
 * 
 * 
 */
public final class PageReference {

    /** In-memory deserialized page instance. */
    private AbstractPage mPage;

    /** Corresponding mKey of the related node page. */
    private long mNodePageKey = -1;

    /** Key in persistent storage. */
    private AbsKey mKey;

    /** Checksum of serialized page. */
    private byte[] mChecksum = new byte[IConstants.CHECKSUM_SIZE];

    /**
     * Default constructor setting up an uninitialized page reference.
     */
    public PageReference() {
        this(null, null, new byte[IConstants.CHECKSUM_SIZE]);
    }

    /**
     * Constructor to clone an existing page reference.
     * 
     * @param pageReference
     *            Page reference to clone.
     */
    public PageReference(final PageReference pageReference) {
        this(pageReference.mPage, pageReference.mKey, pageReference.mChecksum);
    }

    /**
     * Constructor to properly set up a page reference.
     * 
     * @param page
     *            In-memory deserialized page instance.
     * @param key
     *            {@link AbsKey} of the page to be referenced in the
     *            persistent storage
     * @param checksum
     *            Checksum of serialized page.
     */
    public PageReference(final AbstractPage page, final AbsKey key, final byte[] checksum) {
        mPage = page;
        mKey = key;
        System.arraycopy(checksum, 0, mChecksum, 0, IConstants.CHECKSUM_SIZE);
    }

    /**
     * Read page reference from storage.
     * 
     * @param mIn
     *            Input bytes.
     */
    public PageReference(final ITTSource mIn) {
        mPage = null;
        mKey = KeyPersistenter.createKey(mIn);
        mChecksum = new byte[IConstants.CHECKSUM_SIZE];
        for (int i = 0; i < mChecksum.length; i++) {
            mChecksum[i] = mIn.readByte();
        }
    }

    /**
     * Is there an instantiated page?
     * 
     * @return True if the reference points to an in-memory instance.
     */
    public boolean isInstantiated() {
        return (mPage != null);
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
     * Get the checksum of the serialized page.
     * 
     * @param checksum
     *            getting the checksum of the page in this byte array
     */
    public void getChecksum(final byte[] checksum) {
        System.arraycopy(mChecksum, 0, checksum, 0, IConstants.CHECKSUM_SIZE);
    }

    /**
     * Set the checksum of the serialized page.
     * 
     * @param checksum
     *            Checksum of serialized page.
     */
    public void setChecksum(final byte[] checksum) {
        System.arraycopy(checksum, 0, mChecksum, 0, IConstants.CHECKSUM_SIZE);
    }

    /**
     * Get in-memory instance of deserialized page.
     * 
     * @return In-memory instance of deserialized page.
     */
    public AbstractPage getPage() {
        return mPage;
    }

    /**
     * Set in-memory instance of deserialized page.
     * 
     * @param page
     *            Deserialized page.
     */
    public void setPage(final AbstractPage page) {
        mPage = page;
    }

    /**
     * Get start byte offset in file.
     * 
     * @return Start offset in file.
     */
    public AbsKey getKey() {
        return mKey;
    }

    /**
     * Set start byte offset in file.
     * 
     * @param key
     *            Key of this reference set by the persistent storage
     */
    public void setKey(final AbsKey key) {
        this.mKey = key;
    }

    /**
     * Serialize page reference to output.
     * 
     * @param mOut
     *            Output bytes that get written to a file.
     */
    public void serialize(final ITTSink mOut) {
        KeyPersistenter.serializeKey(mOut, mKey);
        for (final byte byteVal : mChecksum) {
            mOut.writeByte(byteVal);
        }
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public boolean equals(final Object object) {
    // if (!(object instanceof PageReference)) {
    // return false;
    // }
    // final PageReference pageReference = (PageReference) object;
    // boolean checksumEquals = true;
    // byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];
    // pageReference.getChecksum(tmp);
    // for (int i = 0; i < IConstants.CHECKSUM_SIZE; i++) {
    // checksumEquals &= (tmp[i] == mChecksum[i]);
    // }
    // boolean keyEquals = mKey == pageReference.mKey;
    // return (checksumEquals && keyEquals);
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public int hashCode() {
    //
    // }

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
        builder.append(", checksum");
        builder.append(mChecksum);
        builder.append(", page=(");
        builder.append(mPage);
        builder.append(")");
        return builder.toString();
    }

    /**
     * @param nodePageKey
     *            the nodePageKey to set
     */
    public void setNodePageKey(long nodePageKey) {
        this.mNodePageKey = nodePageKey;
    }

    /**
     * @return the nodePageKey
     */
    public long getNodePageKey() {
        return mNodePageKey;
    }
    
}
