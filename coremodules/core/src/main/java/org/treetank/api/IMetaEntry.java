/**
 * 
 */
package org.treetank.api;

/**
 * All entries in the MetaPage must implement this interface for guaranteeing serialization.
 * This applies to Keys as well as to values.
 * 
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
