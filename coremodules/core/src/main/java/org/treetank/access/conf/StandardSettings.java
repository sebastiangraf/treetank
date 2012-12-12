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
import org.jclouds.imagestore.ImageStoreConstants;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
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
        install(new FactoryModuleBuilder().implement(IBackend.class, FileStorage.class).build(
            IBackendFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        bind(Key.class).toInstance(KEY);
        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
    }

    public static Properties getStandardProperties(final String pathToStorage, final String resource) {
        Properties properties = new Properties();
        properties.setProperty(ContructorProps.STORAGEPATH, pathToStorage);
        properties.setProperty(ContructorProps.RESOURCE, resource);
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, new File(new File(new File(properties
            .getProperty(ContructorProps.STORAGEPATH), StorageConfiguration.Paths.Data.getFile().getName()),
            properties.getProperty(ContructorProps.RESOURCE)), ResourceConfiguration.Paths.Data.getFile()
            .getName()).getAbsolutePath());
        properties.setProperty(ContructorProps.NUMBERTORESTORE, Integer.toString(4));
        properties.setProperty(Constants.PROPERTY_CREDENTIAL, "test");
        properties.setProperty(ContructorProps.JCLOUDSTYPE, "filesystem");
        // Class name for painter for imagehost
        properties.setProperty(ImageStoreConstants.PROPERTY_BYTEPAINTER,
            "org.jclouds.imagestore.imagegenerator.bytepainter.HexadecimalBytesToImagePainter");
        // Class name for imagehost
        properties.setProperty(ImageStoreConstants.PROPERTY_IMAGEHOSTER,
            "org.jclouds.imagestore.imagehoster.file.ImageHostFile");
        // properties.setProperty(ImageStoreConstants.PROPERTY_IMAGEHOSTER,
        // "org.jclouds.imagestore.imagehoster.flickr.ImageHostFlickr");
        return properties;
    }
}
