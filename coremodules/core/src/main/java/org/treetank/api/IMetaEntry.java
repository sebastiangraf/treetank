/**
 * 
 */
package org.treetank.api;

import java.io.DataOutput;

import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;

/**
 * All entries in the MetaBucket must implement this interface for guaranteeing serialization.
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

    /**
     * Getting a Funnel for computing guava-based hashes.
     * 
     * @return a Funnel for this {@link IMetaEntry}
     */
    Funnel<IMetaEntry> getFunnel();

}
