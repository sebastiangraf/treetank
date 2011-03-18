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
 * Interface for providing byteAccess to the write-process in the storage. That
 * means that every serialization process in TreeTank is using this interface
 * and that the related concrete storage implementation is implementing this
 * interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ITTSink {

    /**
     * Writing a long to the storage.
     * 
     * @param mLongVal
     *            to be written
     */
    void writeLong(final long mLongVal);

    /**
     * Writing an int to the storage.
     * 
     * @param mIntVal
     *            to be written
     */
    void writeInt(final int mIntVal);

    /**
     * Writing a byte to the storage.
     * 
     * @param mByteVal
     *            to be written
     */
    void writeByte(final byte mByteVal);

}
