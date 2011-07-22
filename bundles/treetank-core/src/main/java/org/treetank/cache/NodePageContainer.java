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

package org.treetank.cache;

import com.sleepycat.bind.tuple.TupleOutput;

import org.treetank.io.berkeley.TupleOutputSink;
import org.treetank.page.NodePage;
import org.treetank.page.PagePersistenter;

/**
 * <h1>NodePageContainer</h1> This class acts as a container for revisioned {@link NodePage}s. Each
 * {@link NodePage} is stored in a versioned manner. If
 * modifications occur, the versioned {@link NodePage}s are dereferenced and
 * reconstructed. Afterwards, this container is used to store a complete {@link NodePage} as well as one for
 * upcoming modifications.
 * 
 * Both {@link NodePage}s can differ since the complete one is mainly used for
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
     * @param paramComplete
     *            to be used as a base for this container.
     */
    public NodePageContainer(final NodePage paramComplete) {
        this(paramComplete, new NodePage(paramComplete.getNodePageKey(), paramComplete.getRevision()));
    }

    /**
     * Constructor with both, complete and modifying page.
     * 
     * @param paramComplete
     *            to be used as a base for this container
     * @param paramModifying
     *            to be used as a base for this container
     */
    public NodePageContainer(final NodePage paramComplete, final NodePage paramModifying) {
        this.mComplete = paramComplete;
        this.mModified = paramModifying;
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
     * @param paramOut
     *            for serialization
     */
    public void serialize(final TupleOutput paramOut) {
        final TupleOutputSink sink = new TupleOutputSink(paramOut);
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
        final StringBuilder builder = new StringBuilder("Pagekey: ");
        builder.append(mComplete.getNodePageKey());
        builder.append("\nComplete page: ");
        builder.append(mComplete.toString());
        builder.append("\nModified page: ");
        builder.append(mModified.toString());
        return builder.toString();

    }

}
