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

package org.treetank.io;

/**
 * Abstract class to provide a key corresponding to the storage. A Key is the
 * link to the persistent representation in the physical database e.g. the
 * offset in a file or the key in a relational mapping.
 * 
 * More than one keys are possible if necessary e.g. related to the
 * file-layer-implementation: the offset plus the length. Only one key must be
 * unique.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbsKey {

    /** all keys. */
    private final transient long[] mKeys;

    /**
     * Protected constructor, just setting the keys.
     * 
     * @param paramKeys
     *            setting the keys.
     */
    protected AbsKey(final long... paramKeys) {
        mKeys = paramKeys;
    }

    /**
     * Getting all keys.
     * 
     * @return the keys
     */
    protected final long[] getKeys() {
        final long[] returnKeys = new long[mKeys.length];
        System.arraycopy(mKeys, 0, returnKeys, 0, mKeys.length);
        return returnKeys;
    }

    /**
     * Getting the primary one.
     * 
     * @return the key which is profmaly
     */
    public abstract long getIdentifier();
}
