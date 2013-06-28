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

    /**
     * Getting the HashValues mapped to the references guarding the integrity of the referenced buckets.
     * 
     * @return an array of checksums from the referenced buckets
     */
//    int[] getReferenceHashs();

    /**
     * Setting the hash of a referenced bucket to this bucket.
     * 
     * @param pIndex
     *            where the hash should be applied to
     * @param pHash
     *            to be stored in
     */
//    void setReferenceHash(int pIndex, int pHash);

}
