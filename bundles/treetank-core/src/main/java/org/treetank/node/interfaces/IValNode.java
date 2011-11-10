/**
 * 
 */
package org.treetank.node.interfaces;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IValNode extends INode {

    /**
     * Return a byte array representation of the item's value.
     * 
     * @return returns the value of the item
     */
    byte[] getRawValue();

    /**
     * Setting the value key.
     * 
     * @param paramUriKey
     *            the urikey to be set.
     * @param paramVal
     *            the value to be set.
     */
    void setValue(byte[] paramVal);

}
