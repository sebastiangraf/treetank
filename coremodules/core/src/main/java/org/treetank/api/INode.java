/**
 * 
 */
package org.treetank.api;

/**
 * Overall {@link INode}-Interface for the interaction with the page-layer. All
 * persistence functionality must be handled over this interface while all
 * node-layers interfaces interhit from this interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface INode {

    /**
     * Getting the byte representation of the {@link INode}.
     * 
     * @return the byte representation of this {@link INode}
     */
    byte[] getByteRepresentation();

    /**
     * Sets unique {@link INode} key.
     * 
     * 
     * @param pNodeKey
     *            Unique key of item.
     */
    void setNodeKey(final long pNodeKey);

    /**
     * Gets unique {@link INode} key.
     * 
     * @return node key
     */
    long getNodeKey();

    /**
     * Setting the actual hash of the structure.
     * 
     * @param pHash
     *            hash to be set for this {@link INode}
     * 
     */
    void setHash(final long pHash);

    /**
     * Getting the persistent stored hash.
     * 
     * @return the hash of this {@link INode}
     */
    long getHash();

}
