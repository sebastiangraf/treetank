/**
 * 
 */
package org.treetank.api;

/**
 * Creating MetaEntries based on the implementation and the bundle.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IMetaEntryFactory {

    /**
     * Create a meta-entry out of a serialized byte-representation.
     * 
     * @param pData
     *            byte representation.
     * @return the created metaEntry.
     */
    IMetaEntry deserializeEntry(final byte[] pData);

}
