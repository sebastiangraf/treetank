/**
 * 
 */
package org.treetank.node;

import org.treetank.utils.NamePageHash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Constants for the Node Layer.
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IConstants {

    /** Static to determine key for root node. */
    public static final long ROOT_NODE = 0;
    /** Static to determine key for null node. */
    public static final long NULL_NODE = -1;

    /** Hashing function for nodes. */
    public static final HashFunction HF = Hashing.md5();

    /** Static type key for normal txpes. */
    public static final int TYPE_KEY = NamePageHash.generateHashForString("xs:untyped");

}
