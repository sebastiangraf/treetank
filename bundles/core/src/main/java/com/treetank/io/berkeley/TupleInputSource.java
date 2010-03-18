package com.treetank.io.berkeley;

import com.sleepycat.bind.tuple.TupleInput;
import com.treetank.io.ITTSource;

/**
 * {@link ITTSource} implementation for the BerkeleyDB-Layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TupleInputSource implements ITTSource {

    /** {@link TupleInput} to be wrapped */
    private transient final TupleInput input;

    /**
     * Constructor.
     * 
     * @param paramInput
     *            to be wrapped
     */
    public TupleInputSource(final TupleInput paramInput) {
        input = paramInput;
    }

    /**
     * {@inheritDoc}
     */
    public byte readByte() {
        return input.readByte();
    }

    /**
     * {@inheritDoc}
     */
    public long readLong() {
        return input.readLong();
    }

    /**
     * {@inheritDoc}
     */
    public int readInt() {
        return input.readInt();
    }

}
