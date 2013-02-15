/**
 * 
 */
package org.treetank.api;

import java.io.DataInput;

import org.treetank.exception.TTIOException;
import org.treetank.page.DumbNodeFactory;

import com.google.inject.ImplementedBy;

/**
 * Factory to generate nodes out of raw data. Depending on the kind of nodes, this factory simple generate
 * nodes for the {@link INode} interface.
 * 
 * The interface is the counterpart to the serialization-method
 * {@link org.treetank.api.INode#getByteRepresentation()}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(DumbNodeFactory.class)
public interface INodeFactory {

    /**
     * Create a node out of a serialized byte-representation.
     * 
     * @param pData
     *            byte representation.
     * @return the created node.
     */
    INode deserializeNode(final DataInput pData) throws TTIOException;

}
