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

package com.treetank.io.berkeley;

import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.ITTSink;

/**
 * Simple wrapper as an {@link ITTSink} implemetation.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TupleOutputSink implements ITTSink {

    /** {@link TupleOutput} to be wrapped. */
    private transient final TupleOutput mOutput;

    /**
     * Constructor.
     * 
     * @param paramOutput
     *            to be wrapped
     */
    public TupleOutputSink(final TupleOutput paramOutput) {
        this.mOutput = paramOutput;
    }

    /**
     * {@inheritDoc}
     */
    public void writeByte(final byte mByteVal) {
        mOutput.writeByte(mByteVal);
    }

    /**
     * {@inheritDoc}
     */
    public void writeLong(final long mLongVal) {
        mOutput.writeLong(mLongVal);
    }

    /**
     * {@inheritDoc}
     */
    public void writeInt(final int mIntVal) {
        mOutput.writeInt(mIntVal);
    }

}
