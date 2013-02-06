package org.treetank.access.test;

import java.security.Key;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.GuiSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.revisioning.Differential;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Andreas Rain
 * 
 */
public class FileModuleFactory implements IModuleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {
        
        AbstractModule returnVal;
        String suiteName = context.getSuite().getName();
        switch (suiteName) {
        case "JCloudsZipper":
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(INodeFactory.class).to(FileNodeFactory.class);
                    bind(IMetaEntryFactory.class).to(FilelistenerMetaPageFactory.class);
                    bind(IRevisioning.class).to(Differential.class);
                    bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));

                    install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
                        IBackendFactory.class));

                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

                    bind(Key.class).toInstance(StandardSettings.KEY);
                    install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
                }
            };
            break;
        case "JCloudsEncryptor":
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(INodeFactory.class).to(FileNodeFactory.class);
                    bind(IMetaEntryFactory.class).to(FilelistenerMetaPageFactory.class);
                    bind(IRevisioning.class).to(Differential.class);
                    bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));

                    install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
                        IBackendFactory.class));

                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

                    bind(Key.class).toInstance(StandardSettings.KEY);
                    install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
                }
            };
            break;
        case "BerkeleyZipper":
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(INodeFactory.class).to(FileNodeFactory.class);
                    bind(IMetaEntryFactory.class).to(FilelistenerMetaPageFactory.class);
                    bind(IRevisioning.class).to(Differential.class);
                    bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));

                    install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
                        .build(IBackendFactory.class));

                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

                    bind(Key.class).toInstance(StandardSettings.KEY);
                    install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
                }
            };
            break;
        case "BerkeleyEncryptor":
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(INodeFactory.class).to(FileNodeFactory.class);
                    bind(IMetaEntryFactory.class).to(FilelistenerMetaPageFactory.class);
                    bind(IRevisioning.class).to(Differential.class);
                    bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));

                    install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
                        .build(IBackendFactory.class));

                    install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

                    bind(Key.class).toInstance(StandardSettings.KEY);
                    install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
                }
            };
            break;

        default:
            returnVal = new GuiSettings(JCloudsStorage.class);
            break;
        }

        return returnVal;
    }
}
