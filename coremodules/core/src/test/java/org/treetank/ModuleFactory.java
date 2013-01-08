package org.treetank;

import java.security.Key;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeFactory;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.Differential;
import org.treetank.revisioning.IRevisioning;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;

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
public class ModuleFactory implements IModuleFactory {

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
                    install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class)
                        .build(IRevisioningFactory.class));

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
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
                    install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class)
                        .build(IRevisioningFactory.class));

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
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
                    install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class)
                        .build(IRevisioningFactory.class));

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
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
                    install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class)
                        .build(IRevisioningFactory.class));

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
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
            returnVal = new StandardSettings();
            break;
        }

        return returnVal;
    }
}
