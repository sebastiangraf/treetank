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
package com.treetank.diff;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class Depth {
    private transient int mNewDepth;
    
    private transient int mOldDepth;
    
    void incrementNewDepth() {
        mNewDepth++;
    }
    
    void decrementNewDepth() {
        mNewDepth--;
    }
    
    void incrementOldDepth() {
        mOldDepth++;
    }
    
    void decrementOldDepth() {
        mOldDepth--;
    }
    
    int getNewDepth() {
        return mNewDepth;
    }
    
    int getOldDepth() {
        return mOldDepth;
    }
}
