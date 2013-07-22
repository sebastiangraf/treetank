/**
 * 
 */
package org.treetank.api;

import java.io.DataOutput;

import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;

/**
 * Overall {@link IData}-Interface for the interaction with the bucket-layer. All
 * persistence functionality must be handled over this interface while all
 * data-layers interfaces inherit from this interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IData {

    /**
     * Serializing to given dataput
     * 
     * @param pOutput
     *            to serialize to
     * @throws TTIOException
     */
    void serialize(final DataOutput pOutput) throws TTIOException;

    /**
     * Gets unique {@link IData} key.
     * This key should be set over the <code>IBucketWriteTrx.incrementDataKey</code> for getting the correct
     * offset
     * within retrievals.
     * 
     * @return datakey
     */
    long getDataKey();

    /**
     * Getting a Funnel for computing guava-based hashes.
     * 
     * @return a Funnel for this {@link IData}
     */
    Funnel<IData> getFunnel();

}
