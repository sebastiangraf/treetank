/**
 * 
 */
package org.treetank.api;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface INodeFactory {

    /**
     * Create page.
     * 
     * @param paramSource
     *            source to read from
     * @return the created page
     */
    INode deserializeNode(final byte[] pData);

}
