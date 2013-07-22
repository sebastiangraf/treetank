/**
 * 
 */
package org.treetank.api;

import java.io.DataInput;

import org.treetank.bucket.DumbDataFactory;
import org.treetank.exception.TTIOException;

import com.google.inject.ImplementedBy;

/**
 * Factory to generate datas out of raw data. Depending on the kind of data, this factory simple generate
 * datas for the {@link IData} interface.
 * 
 * The interface is the counterpart to the serialization-method
 * {@link org.treetank.api.IData#serialize(java.io.DataOutput)}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(DumbDataFactory.class)
public interface IDataFactory {

    /**
     * Create a data out of a serialized byte-representation.
     * 
     * @param pData
     *            byte representation.
     * @return the created data.
     * @throws TTIOException
     *             if any weird happens
     */
    IData deserializeData(final DataInput pData) throws TTIOException;

}
