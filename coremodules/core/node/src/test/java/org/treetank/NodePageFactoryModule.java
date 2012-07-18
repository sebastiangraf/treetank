/**
 * 
 */
package org.treetank;

import org.treetank.api.INodeFactory;
import org.treetank.node.NodeFactory;

import com.google.inject.AbstractModule;

/**
 * Module for binding node-layer to page-layer
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodePageFactoryModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(INodeFactory.class).to(NodeFactory.class);
    }

}
