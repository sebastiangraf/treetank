/**
 * 
 */
package org.treetank.api;

import org.treetank.bucket.MetaBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * Read-Transaction of a bucket ensuring read-only access to any buckets.
 * The transaction must be bound to a revision and bases on a session.
 * 
 * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IBucketReadTrx pRtx = session.beginBucketRtx(REVISION);
 * </code>
 * 
 * Each {@link IBucketReadTrx} can afterwards get datas from the bucket-layer and the underlaying backend.
 * Note that each session furthermore has access to a centralized store of common strings referencable over a
 * key.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IBucketReadTrx {

    /**
     * Getting the data related to a key.
     * 
     * @param pKey
     *            the key of the data
     * @return a suitable {@link IData}
     * @throws TTException
     *             if anything weird happens
     */
    IData getData(final long pKey) throws TTIOException;

    /**
     * Getting the revision number of this transaction
     * 
     * @return the revision number of this transaction
     * @throws TTIOException
     *             if deserialization fails.
     * 
     */
    long getRevision() throws TTIOException;

    /**
     * Close the transaction.
     * 
     * @return true if successful, false otherwise
     * @throws TTIOException
     *             if anything weird happens
     */
    boolean close() throws TTIOException;

    /**
     * Check if transaction is closed.
     * 
     * @return true if closed, false otherwise
     */
    boolean isClosed();

    /**
     * Getting the map with the entire mapping for retrieving meta-information related to the bundle.
     * 
     * @return the meta bucket
     */
    MetaBucket getMetaBucket();

}
