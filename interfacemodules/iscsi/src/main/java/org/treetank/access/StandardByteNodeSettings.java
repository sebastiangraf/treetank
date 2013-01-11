package org.treetank.access;

import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.iscsi.node.ByteNodeFactory;
import org.treetank.iscsi.node.ISCSIMetaPageFactory;

/**
 * Standard Module defining standard settings.
 * 
 * @author Andreas Rain
 * 
 */
public class StandardByteNodeSettings extends StandardSettings {

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(ByteNodeFactory.class);
        bind(IMetaEntryFactory.class).to(ISCSIMetaPageFactory.class);
        configureNormal();
    }

}
