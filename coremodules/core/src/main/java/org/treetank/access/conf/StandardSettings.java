/**
 * 
 */
package org.treetank.access.conf;

import java.io.File;
import java.security.Key;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import org.jclouds.Constants;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IConstants;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.jclouds.JCloudsStorage;
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

    private static byte[] keyValue = new byte[] {
        'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k'
    };
    public static final Key KEY;
    static {
        KEY = new SecretKeySpec(keyValue, "AES");
    }

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(DumbNodeFactory.class);
        configureNormal();
    }

    public void configureNormal() {
        install(new FactoryModuleBuilder().implement(IRevisioning.class, Differential.class).build(
            IRevisioningFactory.class));
        bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        install(new FactoryModuleBuilder().implement(IStorage.class, JCloudsStorage.class).build(
            IStorageFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));

        bind(Key.class).toInstance(KEY);
        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
    }

    public static Properties getStandardProperties(final String mDatabase, final String resource) {
        Properties properties = new Properties();
        properties.setProperty(IConstants.DBFILE, mDatabase);
        properties.setProperty(IConstants.RESOURCE, resource);
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, new File(new File(new File(properties
            .getProperty(IConstants.DBFILE), DatabaseConfiguration.Paths.Data.getFile().getName()),
            properties.getProperty(IConstants.RESOURCE)), ResourceConfiguration.Paths.Data.getFile()
            .getName()).getAbsolutePath());
        properties.setProperty(Constants.PROPERTY_CREDENTIAL, "test");
        properties.setProperty(IConstants.JCLOUDSTYPE, "filesystem");
        return properties;
    }
}
