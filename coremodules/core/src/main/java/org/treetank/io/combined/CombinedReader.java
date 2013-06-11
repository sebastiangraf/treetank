package org.treetank.io.combined;

import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;

/**
 * 
 * Reader for a combined storage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class CombinedReader implements IBackendReader {

    /** First reader. */
    private final IBackendReader mFirstReader;

    /** Second reader. */
    private final IBackendReader mSecondReader;

    /**
     * Constructor.
     * 
     * @param pFirstReader
     *            First reader to read the data from
     * @param pSecondReader
     *            Second reader to read the data from, fallback from the first reader
     */
    public CombinedReader(final IBackendReader pFirstReader, final IBackendReader pSecondReader) {
        this.mFirstReader = pFirstReader;
        this.mSecondReader = pSecondReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBucket read(long pKey) throws TTIOException {
        IBucket returnVal = mFirstReader.read(pKey);
        if (returnVal == null) {
            returnVal = mSecondReader.read(pKey);
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        UberBucket returnVal = mFirstReader.readUber();
        if (returnVal == null) {
            returnVal = mSecondReader.readUber();
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mFirstReader.close();
        mSecondReader.close();
    }

}
