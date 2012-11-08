package org.treetank.access;

import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeFactory;
import org.treetank.iscsi.node.ByteNodeFactory;

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
      configureNormal();
  }
}
