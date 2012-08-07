package org.treetank;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeFactory;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.FullDump;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeModuleFactory implements IModuleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {

        AbstractModule returnVal = new StandardSettings();
        String suiteName = context.getSuite().getName();
        if ("FileZipper".equals(suiteName)) {
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(IRevisioning.class).to(FullDump.class);

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
                    bind(IByteHandler.class).to(Zipper.class);

                    install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
                        IStorageFactory.class));

                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
                }
            };
        }

        if ("FileEncryptor".equals(suiteName)) {
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(IRevisioning.class).to(FullDump.class);

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
                    bind(IByteHandler.class).to(Encryptor.class);

                    install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
                        IStorageFactory.class));
                    
                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
                }
            };
        }

        return returnVal;
    }

}
