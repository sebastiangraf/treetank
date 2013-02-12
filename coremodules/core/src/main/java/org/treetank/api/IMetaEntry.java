/**
 * 
 */
package org.treetank.api;

import java.io.DataOutput;

import org.treetank.exception.TTIOException;

/**
 * All entries in the MetaPage must implement this interface for guaranteeing serialization.
 * This applies to Keys as well as to values.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IMetaEntry {

    /**
     * Serializing to given dataput
     * 
     * @param pOutput
     *            to serialize to
     * @throws TTIOException
     */
    void serialize(final DataOutput pOutput) throws TTIOException;
}
