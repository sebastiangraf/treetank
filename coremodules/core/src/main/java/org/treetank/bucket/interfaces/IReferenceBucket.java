/**
 * 
 */
package org.treetank.bucket.interfaces;

/**
 * Interface denoting all buckets holding references to other buckets.
 * The references are represented by the keys of the serialized storage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IReferenceBucket extends IBucket {

    /** Guaranteed Indirect Offset from any ReferenceBucket. */
    public static int GUARANTEED_INDIRECT_OFFSET = 0;

    /**
     * Getting the keys of the referenced buckets.
     * 
     * @return the keys for the referenced buckets.
     */
    long[] getReferenceKeys();

    /**
     * Setting one key of a bucket to be referenced from this bucket.
     * 
     * @param pIndex
     *            offset of the key to be referenced
     * @param pKey
     *            the key of the bucket to be referenced
     */
    void setReferenceKey(int pIndex, long pKey);

}
