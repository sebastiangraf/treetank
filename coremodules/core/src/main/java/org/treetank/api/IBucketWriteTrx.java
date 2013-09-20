/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;

/**
 * Write-Transaction of a bucket ensuring read- and write access to any buckets.
 * The transaction is bound on the very last revision and bases on a session.
 * 
 * Each {@link IBucketWriteTrx} can afterwards get datas from the bucket-layer and the underlaying backend as
 * well
 * as store new {@link IData}s in the backend.
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IBucketWriteTrx extends IBucketReadTrx {

    /**
     * Getting most recent data key and incrementing it.
     * 
     * @return the most recent data key.
     */
    long incrementDataKey();

    /**
     * Setting a data and storing the data in the bucket-layer.
     * 
     * @param pData
     *            the data to be stored.
     * @throws TTException
     *             if anything weird happens.
     */
    long setData(IData pData) throws TTException;

    /**
     * Removing the data from the storage.
     * 
     * @param pData
     *            to be removed
     * @throws TTException
     */
    void removeData(final IData pData) throws TTException;

    /**
     * Simple commit of this bucket transaction to store the newest version.
     * 
     * @throws if
     *             anything weird happens
     */
    void commit() throws TTException;

    /**
     * Simple commit blocked of this bucket transaction to store the newest version.
     * 
     * @throws if
     *             anything weird happens
     */
    void commitBlocked() throws TTException;

}
