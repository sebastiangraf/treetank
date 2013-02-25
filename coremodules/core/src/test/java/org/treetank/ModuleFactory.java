package org.treetank;

import java.net.URL;
import java.security.Key;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.combinedCloud.CombinedBackend;
import org.treetank.revisioning.Differential;
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
public class ModuleFactory implements IModuleFactory {

    private final static String NODEFACTORYPARAMTER = "NodeFactory";
    private final static String METAFACTORYPARAMTER = "MetaFactory";

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {
        AbstractModule returnVal = null;

        if (context.getSuite().getParameter(NODEFACTORYPARAMTER) != null) {
            final String nodeFacName = context.getSuite().getParameter(NODEFACTORYPARAMTER);
            final String metaFacName = context.getSuite().getParameter(METAFACTORYPARAMTER);
            try {
                final Class<INodeFactory> nodeFac = (Class<INodeFactory>)Class.forName(nodeFacName);
                final Class<IMetaEntryFactory> metaFac = (Class<IMetaEntryFactory>)Class.forName(metaFacName);
                returnVal = new AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(INodeFactory.class).to(nodeFac);
                        bind(IMetaEntryFactory.class).to(metaFac);
                        bind(IRevisioning.class).to(Differential.class);
                        bind(IByteHandlerPipeline.class).toInstance(
                            new ByteHandlerPipeline(new Zipper(), new Encryptor(StandardSettings.KEY)));
                        install(new FactoryModuleBuilder().implement(IBackend.class, CombinedBackend.class)
                            .build(IBackendFactory.class));
                        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
                        bind(Key.class).toInstance(StandardSettings.KEY);
                        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
                    }
                };

            } catch (ClassNotFoundException exc) {
                throw new RuntimeException(exc);
            }
        } else {

            URL bla = testClass.getProtectionDomain().getCodeSource().getLocation();

            returnVal = new StandardSettings();
        }

        return returnVal;

        // String suiteName = context.getSuite().getName();
        // switch (suiteName) {
        // case "JCloudsZipper":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
        // IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "JCloudsEncryptor":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
        // IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "BerkeleyZipper":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
        // .build(IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "BerkeleyEncryptor":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
        // .build(IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;

        // default:
        // break;
        // }

    }
}
