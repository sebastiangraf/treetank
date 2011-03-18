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

package org.treetank.io.file;

import org.treetank.io.AbsKey;
import org.treetank.io.ITTSource;

/**
 * FileKey, storing the offset and the length. The key is used for the mapping
 * between PageReerence and Page.
 * 
 * @author Sebastian Graf, University of Konstnz
 * 
 */
public final class FileKey extends AbsKey {

    /**
     * Constructor for {@link ITTSource}.
     * 
     * @param mInSource
     *            Source for Input
     */
    public FileKey(final ITTSource mInSource) {
        super(mInSource.readLong(), mInSource.readLong());
    }

    /**
     * Constructor for direct data.
     * 
     * @param mOffset
     *            Offset of data
     * @param mLength
     *            Length of data
     */
    public FileKey(final long mOffset, final long mLength) {
        super(mOffset, mLength);
    }

    /**
     * Getting the length of the file fragment.
     * 
     * @return the length of the file fragment
     */
    public int getLength() {
        return (int)super.getKeys()[1];
    }

    /**
     * Getting the offset of the file fragment.
     * 
     * @return the offset
     */
    public long getOffset() {
        return super.getKeys()[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIdentifier() {
        return super.getKeys()[0];
    }

}
