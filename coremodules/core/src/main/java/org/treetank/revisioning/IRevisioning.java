/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.bucket.NodeBucket;
import org.treetank.io.LogValue;

/**
 * This interface offers methods to revision data differently.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */

public interface IRevisioning {

    /**
     * Method to reconstruct a complete {@link NodeBucket} with the help of party filled
     * buckets plus a revision-delta which determines the necessary steps back.
     * 
     * @param pBuckets
     *            the base of the complete {@link NodeBucket}
     * @return the complete {@link NodeBucket}
     */
    NodeBucket combineBuckets(final NodeBucket[] pBuckets);

    /**
     * Method to reconstruct a complete {@link NodeBucket} for reading as well as a
     * NodeBucket for serializing with the Nodes to write already on there.
     * 
     * @param pRevisionsToRestore
     *            number of revisions to restore
     * @param pNewBucketKey
     *            bucket key of the new bucket
     * @param pBuckets
     *            the base of the complete Nodebucket
     * @param pFullDump
     *            boolean if entire bucket should be written. Must be triggered from extern since it is based on
     *            the revisionToRestore-Param
     * @return a {@link LogValue} holding a complete {@link NodeBucket} for reading a one
     *         for writing
     */
    LogValue combineBucketsForModification(final int pRevisionsToRestore, final long pNewBucketKey,
        final NodeBucket[] pBuckets, final boolean pFullDump);

}
