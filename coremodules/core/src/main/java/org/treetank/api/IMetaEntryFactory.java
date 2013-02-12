/**
 * 
 */
package org.treetank.api;

import java.io.DataInput;

import org.treetank.exception.TTIOException;

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
     * @throws TTIOException
     *             if anything weird happens
     */
    IMetaEntry deserializeEntry(final DataInput pData) throws TTIOException;

}
