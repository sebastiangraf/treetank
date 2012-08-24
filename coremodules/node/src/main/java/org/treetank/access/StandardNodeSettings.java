/**
 * 
 */
package org.treetank.access;

import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeFactory;
import org.treetank.node.TreeNodeFactory;

/**
 * Standard Module defining standard settings.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class StandardNodeSettings extends StandardSettings {

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(TreeNodeFactory.class);
        configureNormal();
    }
}
