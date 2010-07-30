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

package com.treetank.io;

/**
 * Interface for providing byteAccess to the read-process in the storage. That
 * means that every initialisation process in TreeTank from the concrete storage
 * is using this interface and that the related concrete storage implementation
 * is implementing this interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ITTSource {

    /**
     * Reading a long to the storage.
     * 
     * @return the next long of the source
     */
    long readLong();

    /**
     * Reading an byte to the storage.
     * 
     * @return the next byte of the source
     */
    byte readByte();

    /**
     * Reading an int to the storage.
     * 
     * @return the next int of the source
     */
    int readInt();

}
