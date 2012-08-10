/**
 * 
 */
package org.treetank.access.conf;

import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.Differential;
import org.treetank.revisioning.IRevisioning;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;

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
        install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class).build(
            IRevisioningFactory.class));

        bind(INodeFactory.class).to(DumbNodeFactory.class);
        bind(IByteHandler.class).to(Zipper.class);

        install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
            IStorageFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

    }

}
