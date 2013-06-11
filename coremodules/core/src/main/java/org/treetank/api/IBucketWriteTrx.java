/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;

/**
 * Write-Transaction of a bucket ensuring read- and write access to any buckets.
 * The transaction is bound on the very last revision and bases on a session.
 * 
 * Each {@link IBucketWriteTrx} can afterwards get nodes from the bucket-layer and the underlaying backend as well
 * as store new {@link INode}s in the backend.
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IBucketWriteTrx extends IBucketReadTrx {

    /**
     * Getting most recent node key and incrementing it.
     * 
     * @return the most recent node key.
     */
    long incrementNodeKey();

    /**
     * Setting a node and storing the node in the bucket-layer.
     * 
     * @param pNode
     *            the node to be stored.
     * @throws TTException
     *             if anything weird happens.
     */
    long setNode(INode pNode) throws TTException;

    /**
     * Removing the node from the storage.
     * 
     * @param pNode
     *            to be removed
     * @throws TTException
     */
    void removeNode(final INode pNode) throws TTException;

    /**
     * Simple commit of this bucket transaction to store the newest version.
     * 
     * @throws if
     *             anything weird happens
     */
    void commit() throws TTException;

}
