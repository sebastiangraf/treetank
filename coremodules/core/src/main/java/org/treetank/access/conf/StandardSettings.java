/**
 * 
 */
package org.treetank.access.conf;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.security.Key;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import org.jclouds.Constants;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.combinedCloud.CombinedBackend;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.page.DumbMetaEntryFactory;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.Differential;
import org.treetank.revisioning.IRevisioning;

import com.google.common.io.Files;
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
    public static final Key KEY = new SecretKeySpec(keyValue, "AES");

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(DumbNodeFactory.class);
        bind(IMetaEntryFactory.class).to(DumbMetaEntryFactory.class);
        configureNormal();
    }

    public void configureNormal() {
        bind(IRevisioning.class).to(Differential.class);
        bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        install(new FactoryModuleBuilder().implement(IBackend.class, CombinedBackend.class).build(
            IBackendFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        bind(Key.class).toInstance(KEY);
        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
    }

    public static Properties getProps(final String pathToStorage, final String resource) throws TTIOException {
        Properties properties = new Properties();
        properties.setProperty(ConstructorProps.STORAGEPATH, pathToStorage);
        properties.setProperty(ConstructorProps.RESOURCE, resource);
        properties.setProperty(ConstructorProps.RESOURCEPATH, FileSystems.getDefault().getPath(pathToStorage,
            StorageConfiguration.Paths.Data.getFile().getName(), resource).toString());
        properties.setProperty(ConstructorProps.NUMBERTORESTORE, Integer.toString(4));

        // properties.setProperty(ConstructorProps.JCLOUDSTYPE, "aws-s3");
        properties.setProperty(ConstructorProps.JCLOUDSTYPE, "filesystem");
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, Files.createTempDir().getAbsolutePath());

        String[] awsCredentials = getCredentials();
        if (awsCredentials.length == 0) {
            properties.setProperty(Constants.PROPERTY_CREDENTIAL, "test");
        } else {
            properties.setProperty(Constants.PROPERTY_IDENTITY, awsCredentials[0]);
            properties.setProperty(Constants.PROPERTY_CREDENTIAL, awsCredentials[1]);
        }

        // Class name for painter for imagehost
        // properties.setProperty(ImageStoreConstants.PROPERTY_BYTEPAINTER,
        // "org.jclouds.imagestore.imagegenerator.bytepainter.HexadecimalBytesToImagePainter");
        // Class name for imagehost
        // properties.setProperty(ImageStoreConstants.PROPERTY_IMAGEHOSTER,
        // "org.jclouds.imagestore.imagehoster.file.ImageHostFile");
        // properties.setProperty(ImageStoreConstants.PROPERTY_IMAGEHOSTER,
        // "org.jclouds.imagestore.imagehoster.flickr.ImageHostFlickr");

        return properties;

    }

    private static String[] getCredentials() {
        File userStore =
            new File(System.getProperty("user.home"), new StringBuilder(".credentials")
                .append(File.separator).append("aws.properties").toString());
        if (!userStore.exists()) {
            return new String[0];
        } else {
            Properties props = new Properties();
            try {
                props.load(new FileReader(userStore));
                return new String[] {
                    props.getProperty("access"), props.getProperty("secret")
                };

            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }

    }

}
