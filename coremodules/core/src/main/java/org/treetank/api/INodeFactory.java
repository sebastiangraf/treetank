/**
 * 
 */
package org.treetank.api;

import org.treetank.page.DumbNodeFactory;

import com.google.inject.ImplementedBy;

/**
 * Factory to generate nodes out of raw data. Depending on the kind of nodes, this factory simple generate
 * nodes for the {@link INode} interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(DumbNodeFactory.class)
public interface INodeFactory {

    /**
     * Create page.
     * 
     * @param paramSource
     *            source to read from
     * @return the created page
     */
    INode deserializeNode(final byte[] pData);

}
