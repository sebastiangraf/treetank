/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.bucket.DataBucket;
import org.treetank.io.LogValue;

/**
 * This interface offers methods to revision data differently.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */

public interface IRevisioning {

    /**
     * Method to reconstruct a complete {@link DataBucket} with the help of party filled
     * buckets plus a revision-delta which determines the necessary steps back.
     * 
     * @param pBuckets
     *            the base of the complete {@link DataBucket}
     * @return the complete {@link DataBucket}
     */
    DataBucket combineBuckets(final DataBucket[] pBuckets);

    /**
     * Method to reconstruct a complete {@link DataBucket} for reading as well as a
     * DataBucket for serializing with the datas to write already on there.
     * 
     * @param pRevisionsToRestore
     *            number of revisions to restore
     * @param pNewBucketKey
     *            bucket key of the new bucket
     * @param pBuckets
     *            the base of the complete DataBucket
     * @param pFullDump
     *            boolean if entire bucket should be written. Must be triggered from extern since it is based
     *            on
     *            the revisionToRestore-Param
     * @return a {@link LogValue} holding a complete {@link DataBucket} for reading a one
     *         for writing
     */
    LogValue combineBucketsForModification(final int pRevisionsToRestore, final long pNewBucketKey,
        final DataBucket[] pBuckets, final boolean pFullDump);

}
