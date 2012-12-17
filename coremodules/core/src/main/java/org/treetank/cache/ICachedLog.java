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

package org.treetank.cache;

import java.util.Map;

import org.treetank.exception.TTIOException;

/**
 * Interface for all upcoming cache implementations. Can be a weak one, a
 * LRU-based one or a persistent. However, clear, put and get must to be
 * provided. Instances of this class are used with <code> IReadTransactions</code> as well as with
 * <code>IWriteTransactions</code>.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ICachedLog {
    /**
     * Clearing the cache. That is removing all elements.
     * 
     * @throws TTIOException
     *             if clear fails
     */
    void clear() throws TTIOException;

    /**
     * Getting a page related to a given nodepagekey.
     * 
     * @param mKey
     *            the key for the requested {@link NodePageContainer}
     * @return {@link NodePageContainer} instance related to this key
     * @throws TTIOException
     *             if get fails
     */
    NodePageContainer get(final LogKey mKey) throws TTIOException;

    /**
     * Putting an {@link NodePageContainer} into the cache with a corresponding
     * nodepagekey.
     * 
     * @param mKey
     *            for putting the page in the cache.
     * @param mPage
     *            should be putted in the cache as well.
     * @throws TTIOException
     *             if put fails
     */
    void put(final LogKey mKey, final NodePageContainer mPage) throws TTIOException;

    /**
     * Class representing one entry in the transaction-log
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    static class TransactionLogEntry implements Map.Entry<LogKey, NodePageContainer> {

        /** Key of the log. */
        final LogKey mKey;

        /** Container of the log. */
        final NodePageContainer mContainer;

        /**
         * Constructor.
         * 
         */
        public TransactionLogEntry(final LogKey pKey, final NodePageContainer pContainer) {
            mKey = pKey;
            mContainer = pContainer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LogKey getKey() {
            return mKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePageContainer getValue() {
            return mContainer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePageContainer setValue(NodePageContainer value) {
            throw new UnsupportedOperationException();
        }
    }

}
