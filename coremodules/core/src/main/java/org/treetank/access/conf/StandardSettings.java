/**
 * 
 */
package org.treetank.access.conf;

import java.security.Key;

import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
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
        bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
            IStorageFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

        bind(Key.class).toInstance(TestHelper.KEY);
        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));

    }
}
