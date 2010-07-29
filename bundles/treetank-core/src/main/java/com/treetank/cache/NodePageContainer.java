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
package com.treetank.cache;

import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.NodePage;
import com.treetank.page.PagePersistenter;

/**
 * <h1>NodePageContainer</h1> This class acts as a container for revisioned {@link NodePage}s. Each
 * {@link NodePage} is stored in a versioned manner. If
 * modifications occur, the versioned {@link NodePage}s are dereferenced and
 * reconstructed. Afterwards, this container is used to store a complete {@link NodePage} as well as one for
 * upcoming modifications.
 * 
 * Both {@link Nodepage}s can differ since the complete one is mainly used for
 * read access and the modifying one for write access (and therefore mostly lazy
 * dereferenced).
 * 
 * Since objects of this class are stored in a cache, the class has to be
 * serializable.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class NodePageContainer {

    private final NodePage mComplete;

    private final NodePage mModified;

    /**
     * Constructor with complete page and lazy instantiated modifying page.
     * 
     * @param mComplete
     *            to be used as a base for this container.
     */
    public NodePageContainer(final NodePage mComplete) {
        this(mComplete, new NodePage(mComplete.getNodePageKey(), mComplete.getRevision()));
    }

    /**
     * Constructor with both, complete and modifying page.
     * 
     * @param mComplete
     *            to be used as a base for this container
     * @param mModifying
     *            to be used as a base for this container
     */
    public NodePageContainer(final NodePage mComplete, final NodePage mModifying) {
        this.mComplete = mComplete;
        this.mModified = mModifying;
    }

    /**
     * Getting the complete page.
     * 
     * @return the complete page
     */
    public NodePage getComplete() {
        return mComplete;
    }

    /**
     * Getting the modified page.
     * 
     * @return the modified page
     */
    public NodePage getModified() {
        return mModified;
    }

    /**
     * Serializing the container to the cache.
     * 
     * @param mOut
     *            for serialization
     */
    public void serialize(final TupleOutput mOut) {
        final TupleOutputSink sink = new TupleOutputSink(mOut);
        PagePersistenter.serializePage(sink, mComplete);
        PagePersistenter.serializePage(sink, mModified);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mComplete == null) ? 0 : mComplete.hashCode());
        result = prime * result + ((mModified == null) ? 0 : mModified.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object mObj) {
        if (this == mObj) {
            return true;  
        }
            
        if (mObj == null) {
            return false; 
        }
           
        if (getClass() != mObj.getClass()) {
            return false;
        }
            
        final NodePageContainer other = (NodePageContainer)mObj;
        if (mComplete == null) {
            if (other.mComplete != null) {
                return false;
            } 
        } else if (!mComplete.equals(other.mComplete)) {
            return false;
        } else if (!mModified.equals(other.mModified)) {
            return false; 
        }
            
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("NodePageContainer has pagekey =");
        builder.append(mComplete.getNodePageKey());
        builder.append("\nComplete page: ");
        builder.append(mComplete.toString());
        builder.append("\nModified page: ");
        builder.append(mModified.toString());
        return builder.toString();

    }

}
