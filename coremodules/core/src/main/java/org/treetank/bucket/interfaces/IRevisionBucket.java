/**
 * 
 */
package org.treetank.bucket.interfaces;

/**
 * One bucket representing unique buckets within one revision.
 * Normally this should not occur since buckets can be represented by multiple versions in the buckets tree.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IRevisionBucket extends IBucket {

    /**
     * Getting the revision of the bucket.
     * 
     * @return the revision of this bucket.
     */
    long getRevision();

}
