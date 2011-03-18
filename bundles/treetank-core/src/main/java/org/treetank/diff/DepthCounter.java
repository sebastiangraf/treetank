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
package org.treetank.diff;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class DepthCounter {
    /** Depth in new revision. */
    private transient int mNewDepth;
    
    /** Depth in old revision. */
    private transient int mOldDepth;
    
    /** Increment depth in new revision. */
    void incrementNewDepth() {
        mNewDepth++;
    }
    
    /** Decrement depth in new revision. */
    void decrementNewDepth() {
        mNewDepth--;
    }
    
    /** Increment depth in old revision. */
    void incrementOldDepth() {
        mOldDepth++;
    }
    
    /** Decrement depth in old revision. */
    void decrementOldDepth() {
        mOldDepth--;
    }
    
    /** 
     * Get depth in new revision.
     * 
     * @return depth in new revision
     */
    int getNewDepth() {
        return mNewDepth;
    }
    
    /** 
     * Get depth in old revision.
     * 
     * @return depth in old revision
     */
    int getOldDepth() {
        return mOldDepth;
    }
}
