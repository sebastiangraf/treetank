/**
 * 
 */
package org.treetank.service.xml;

import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.node.NodeMetaPageFactory;
import org.treetank.node.TreeNodeFactory;

/**
 * Standard Module defining standard settings.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class StandardXMLSettings extends StandardSettings {

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(TreeNodeFactory.class);
        bind(IMetaEntryFactory.class).to(NodeMetaPageFactory.class);
        configureNormal();
    }

}
