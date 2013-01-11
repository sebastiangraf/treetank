/**
 * 
 */
package org.treetank.api;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IMetaEntry {

    /**
     * Getting the bytes out of an entry of one meta-page-entry.
     * 
     * @return the bytes of this entry.
     */
    byte[] getByteRepresentation();
}
