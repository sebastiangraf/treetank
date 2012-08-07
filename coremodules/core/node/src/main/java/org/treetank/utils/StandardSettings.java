/**
 * 
 */
package org.treetank.utils;

import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
import org.treetank.node.TreeNodeFactory;
import org.treetank.revisioning.FullDump;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Standard Module defining standard settings.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class StandardSettings extends AbstractModule {

    @Override
    protected void configure() {
        bind(IRevisioning.class).to(FullDump.class);

        bind(INodeFactory.class).to(TreeNodeFactory.class);

        bind(IByteHandler.class).to(Zipper.class);

        install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
            IStorageFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        
    }

}
