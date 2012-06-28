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

    // --- Node Kinds
    // ----------------------------------------------------------
    public final static int UNKNOWN = 0;
    public final static int ELEMENT = 1;
    public final static int ATTRIBUTE = 2;
    public final static int TEXT = 3;
    public final static int WHITESPACE = 4;
    public final static int DELETE = 5;
    public final static int PROCESSING = 7;
    public final static int COMMENT = 8;
    public final static int ROOT = 9;
    public final static int NAMESPACE = 13;

    // --- Fixed Key Nodes
    // ----------------------------------------------------------
    /** Static to determine key for root node. */
    public static final long ROOT_NODE = 0;
    /** Static to determine key for null node. */
    public static final long NULL_NODE = -1;

    /** Hashing function for nodes. */
    public static final HashFunction HF = Hashing.md5();

    /** Static type key for normal txpes. */
    public static final int TYPE_KEY = NamePageHash
            .generateHashForString("xs:untyped");

}
