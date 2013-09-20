/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.bucket.DataBucket;
import org.treetank.io.LogValue;

/**
 * Differential versioning of {@link DataBucket}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBucket combineBuckets(final DataBucket[] pBuckets) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pBuckets.length > 0, "At least one Databucket must be provided");
        // create entire buckets..
        final DataBucket returnVal =
            new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer());
        // ...and for all datas...
        for (int i = 0; i < pBuckets[0].getDatas().length; i++) {
            // ..check if data exists in newer version, and if not...
            if (pBuckets[0].getDatas()[i] != null) {
                returnVal.setData(i, pBuckets[0].getData(i));
            }// ...set the version from the last fulldump
            else if (pBuckets.length > 1) {
                returnVal.setData(i, pBuckets[1].getData(i));
            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combineBucketsForModification(int pRevisionsToRestore, long pNewBucketKey,
        DataBucket[] pBuckets, boolean pFullDump) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pBuckets.length > 0, "At least one Databucket must be provided");
        // create buckets for container..
        final DataBucket[] returnVal =
            {
                new DataBucket(pBuckets[0].getBucketKey(), pBuckets[0].getLastBucketPointer()),
                new DataBucket(pNewBucketKey, pBuckets[0].getBucketKey())
            };

        // ...iterate through the datas and check if it is stored..
        for (int j = 0; j < returnVal[0].getDatas().length; j++) {
            // ...check if the data was written within the last version, if so...
            if (pBuckets[0].getData(j) != null) {
                // ...set it in the read and write-version to be rewritten again...
                returnVal[0].setData(j, pBuckets[0].getData(j));
                returnVal[1].setData(j, pBuckets[0].getData(j));
            } else if (pBuckets.length > 1) {
                // otherwise, just store then data from the fulldump to complete read-uucket except...
                returnVal[0].setData(j, pBuckets[1].getData(j));
                // ..a fulldump becomes necessary.
                if (pFullDump) {
                    returnVal[1].setData(j, pBuckets[1].getData(j));
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
