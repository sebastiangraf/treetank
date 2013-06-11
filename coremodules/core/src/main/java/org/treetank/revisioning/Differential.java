/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.bucket.NodeBucket;
import org.treetank.log.LogValue;

/**
 * Differential versioning of {@link NodeBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBucket combineBuckets(final NodeBucket[] pBuckets) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pBuckets.length > 0, "At least one Nodebucket must be provided");
        // create entire buckets..
        final NodeBucket returnVal = new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        // ...and for all nodes...
        for (int i = 0; i < pBuckets[0].getNodes().length; i++) {
            // ..check if node exists in newer version, and if not...
            if (pBuckets[0].getNodes()[i] != null) {
                returnVal.setNode(i, pBuckets[0].getNode(i));
            }// ...set the version from the last fulldump
            else if (pBuckets.length > 1) {
                returnVal.setNode(i, pBuckets[1].getNode(i));
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
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pBuckets.length > 0, "At least one Nodebucket must be provided");
        // create buckets for container..
        final NodeBucket[] returnVal =
            {
                new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new NodeBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };

        // ...iterate through the nodes and check if it is stored..
        for (int j = 0; j < returnVal[0].getNodes().length; j++) {
            // ...check if the node was written within the last version, if so...
            if (pBuckets[0].getNode(j) != null) {
                // ...set it in the read and write-version to be rewritten again...
                returnVal[0].setNode(j, pBuckets[0].getNode(j));
                returnVal[1].setNode(j, pBuckets[0].getNode(j));
            } else if (pBuckets.length > 1) {
                // otherwise, just store then node from the fulldump to complete read-uucket except...
                returnVal[0].setNode(j, pBuckets[1].getNode(j));
                // ..a fulldump becomes necessary.
                if (pFullDump) {
                    returnVal[1].setNode(j, pBuckets[1].getNode(j));
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
