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

    /** {@link TupleOutput} to be wrapped */
    private transient final TupleOutput output;

    /**
     * Constructor.
     * 
     * @param paramOutput
     *            to be wrapped
     */
    public TupleOutputSink(final TupleOutput paramOutput) {
        this.output = paramOutput;
    }

    /**
     * {@inheritDoc}
     */
    public void writeByte(final byte byteVal) {
        output.writeByte(byteVal);
    }

    /**
     * {@inheritDoc}
     */
    public void writeLong(final long longVal) {
        output.writeLong(longVal);
    }

    /**
     * {@inheritDoc}
     */
    public void writeInt(final int intVal) {
        output.writeInt(intVal);
    }

}
