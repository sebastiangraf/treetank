/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.bucket.NodeBucket;
import org.treetank.log.LogValue;

/**
 * Sliding Snapshot versioning of {@link NodeBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SlidingSnapshot implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBucket combineBuckets(final NodeBucket[] pBuckets) {
        checkArgument(pBuckets.length > 0, "At least one Nodebucket must be provided");
        // create entire bucket..
        final NodeBucket returnVal = new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        // ...iterate through the nodes and check if it is stored..
        for (int i = 0; i < pBuckets[0].getNodes().length; i++) {
            boolean bucketSkip = false;
            // ... form the newest version to the oldest one..
            for (int j = 0; !bucketSkip && j < pBuckets.length; j++) {
                // if the node is not set yet but existing in the current version..
                if (pBuckets[j].getNode(i) != null) {
                    // ...break out the loop the next time and..
                    bucketSkip = true;
                    // ...set it
                    returnVal.setNode(i, pBuckets[j].getNode(i));
                }

            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combineBucketsForModification(int pRevisionsToRestore, long pNewBucketKey, NodeBucket[] pBuckets,
        boolean pFullDump) {
        checkArgument(pBuckets.length > 0, "At least one Nodebucket must be provided");
        // create buckets for container..
        final NodeBucket[] returnVal =
            {
                new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new NodeBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };
        // ...iterate through the nodes and check if it is stored..
        for (int i = 0; i < pBuckets[0].getNodes().length; i++) {
            boolean continueVal = true;
            // ... form the newest version to the oldest one..
            for (int j = 0; j < pBuckets.length && continueVal; j++) {
                // check if the node is not set..
                if (returnVal[0].getNode(i) == null && pBuckets[j].getNode(i) != null) {
                    // ...set it to the read-cache and..
                    returnVal[0].setNode(i, pBuckets[j].getNode(i));
                    // ..if we receive the oldest version where the node was not set yet, then copy it by hand
                    if (pBuckets.length >= pRevisionsToRestore && j == pBuckets.length - 1) {
                        returnVal[1].setNode(i, pBuckets[j].getNode(i));
                    }
                    // escape this loop since val was set
                    continueVal = false;
                }
            }
        }
        // return the container
        return new LogValue(returnVal[0], returnVal[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
