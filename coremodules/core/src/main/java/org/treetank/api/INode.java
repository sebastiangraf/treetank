/**
 * 
 */
package org.treetank.api;

/**
 * Overall Node-Interface for the Interaction with the page layer. All
 * persistence functionality must be handled over this interface while all
 * nodeelayer interfaces interhit from this interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface INode {

    /**
     * Getting the byte representation of the node.
     * 
     * @return the byte representation of this node
     */
    byte[] getByteRepresentation();

    /**
     * Sets unique node key.
     * 
     * 
     * @param pNodeKey
     *            Unique key of item, maybe negative when atomics from the
     *            XPath-engine.
     */
    void setNodeKey(final long pNodeKey);

    /**
     * Gets unique node key.
     * 
     * @return node key
     */
    long getNodeKey();

    /**
     * Setting the actual hash of the structure. The hash of one node should
     * have the entire integrity of the related subtree.
     * 
     * @param pHash
     *            hash to be set for this node
     * 
     */
    void setHash(final long pHash);

    /**
     * Getting the persistent stored hash.
     * 
     * @return the hash of this node
     */
    long getHash();

}
