package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.StorageProperties;

/**
 * {@link TupleBinding}-inherting Class to store and retrieve
 * {@link StorageProperties} Objects.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class StoragePropTupleBinding extends
        TupleBinding<StorageProperties> {

    /**
     * {@inheritDoc}
     */
    @Override
    public StorageProperties entryToObject(final TupleInput arg0) {

        final long versionMajor = arg0.readLong();
        final long versionMinor = arg0.readLong();
        final boolean checksummed = arg0.readBoolean();
        final boolean encrypted = arg0.readBoolean();
        return new StorageProperties(versionMajor, versionMinor, checksummed,
                encrypted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectToEntry(final StorageProperties arg0,
            final TupleOutput arg1) {

        arg1.writeLong(arg0.getVersionMajor());
        arg1.writeLong(arg0.getVersionMinor());
        arg1.writeBoolean(arg0.isChecksummed());
        arg1.writeBoolean(arg0.isEncrypted());
    }
}
