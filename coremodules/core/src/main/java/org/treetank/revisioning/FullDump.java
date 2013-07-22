/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;

import org.treetank.bucket.DataBucket;
import org.treetank.io.LogValue;

/**
 * FullDump versioning of {@link DataBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDump implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBucket combineBuckets(DataBucket[] pBuckets) {
        final DataBucket returnVal = new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        for (int i = 0; i < pBuckets[0].getDatas().length; i++) {
            returnVal.setData(i, pBuckets[0].getData(i));
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combineBucketsForModification(int pRevisionsToRestore, long pNewBucketKey, DataBucket[] pBuckets,
        boolean fullDump) {
        final DataBucket[] returnVal =
            {
                new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new DataBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };

        for (int i = 0; i < pBuckets[0].getDatas().length; i++) {
            returnVal[0].setData(i, pBuckets[0].getData(i));
            returnVal[1].setData(i, pBuckets[0].getData(i));
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
