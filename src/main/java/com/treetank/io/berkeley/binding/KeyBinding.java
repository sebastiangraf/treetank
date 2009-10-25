package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.AbstractKey;
import com.treetank.io.KeyPersistenter;
import com.treetank.io.berkeley.BerkeleyKey;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;

/**
 * Class to store {@link AbstractKey} objects (or better {@link BerkeleyKey}
 * objects) in the BerkeleyDB.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class KeyBinding extends TupleBinding<AbstractKey> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKey entryToObject(final TupleInput arg0) {
        return KeyPersistenter.createKey(new TupleInputSource(arg0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectToEntry(final AbstractKey arg0, final TupleOutput arg1) {
        KeyPersistenter.serializeKey(new TupleOutputSink(arg1), arg0);
    }
}
