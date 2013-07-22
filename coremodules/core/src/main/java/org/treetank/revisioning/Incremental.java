/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.bucket.DataBucket;
import org.treetank.io.LogValue;

/**
 * Incremental versioning of {@link DataBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Incremental implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBucket combineBuckets(final DataBucket[] pBuckets) {
        checkArgument(pBuckets.length > 0, "At least one DataBucket must be provided");
        // create entire bucket..
        final DataBucket returnVal = new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        // ...iterate through the datas and check if it is stored..
        for (int i = 0; i < pBuckets[0].getDatas().length; i++) {
            boolean bucketSkip = false;
            // ... form the newest version to the oldest one..
            for (int j = 0; !bucketSkip && j < pBuckets.length; j++) {
                // if the data is not set yet but existing in the current version..
                if (pBuckets[j].getData(i) != null) {
                    // ...break out the loop the next time and..
                    bucketSkip = true;
                    // ...set it
                    returnVal.setData(i, pBuckets[j].getData(i));
                }

            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combineBucketsForModification(int pRevisionsToRestore, long pNewBucketKey, DataBucket[] pBuckets,
        boolean pFullDump) {
        checkArgument(pBuckets.length > 0, "At least one DataBucket must be provided");
        // create buckets for container..
        final DataBucket[] returnVal =
            {
                new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new DataBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };
        // ...iterate through the datas and check if it is stored..
        for (int i = 0; i < pBuckets[0].getDatas().length; i++) {
            boolean bucketSkip = false;
            // ... form the newest version to the oldest one..
            for (int j = 0; !bucketSkip && j < pBuckets.length; j++) {
                // if the data is not set yet but existing in the current version..
                if (pBuckets[j].getData(i) != null) {
                    // ...break out the loop the next time and..
                    bucketSkip = true;
                    // ...set it to the read-cache and..
                    returnVal[0].setData(i, pBuckets[j].getData(i));
                    // ...if a fulldump becomes necessary, set it to the write cache as well.
                    if (pFullDump) {
                        returnVal[1].setData(i, pBuckets[j].getData(i));
                    }
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
