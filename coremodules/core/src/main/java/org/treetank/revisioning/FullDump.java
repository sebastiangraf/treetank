/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;

import org.treetank.bucket.NodeBucket;
import org.treetank.log.LogValue;

/**
 * FullDump versioning of {@link NodeBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDump implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBucket combineBuckets(NodeBucket[] pBuckets) {
        final NodeBucket returnVal = new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        for (int i = 0; i < pBuckets[0].getNodes().length; i++) {
            returnVal.setNode(i, pBuckets[0].getNode(i));
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combineBucketsForModification(int pRevisionsToRestore, long pNewBucketKey, NodeBucket[] pBuckets,
        boolean fullDump) {
        final NodeBucket[] returnVal =
            {
                new NodeBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new NodeBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };

        for (int i = 0; i < pBuckets[0].getNodes().length; i++) {
            returnVal[0].setNode(i, pBuckets[0].getNode(i));
            returnVal[1].setNode(i, pBuckets[0].getNode(i));
        }

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
