/**
 * 
 */
package org.treetank.io.jclouds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.jclouds.Constants;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.filesystem.reference.FilesystemConstants;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class MainApp {

    private static byte[][] vals = new byte[128][128];

    static {
        Random ran = new Random();
        for (int i = 0; i < vals.length; i++) {
            ran.nextBytes(vals[i]);
        }
    }

    public static void main(String[] args) throws IOException {

        // setup where the provider must store the files
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, Files.createTempDir().getAbsolutePath());
        properties.setProperty(Constants.PROPERTY_CREDENTIAL, "test");

        // get a context with filesystem that offers the portable BlobStore api
        BlobStoreContext context = new BlobStoreContextFactory().createContext("filesystem", properties);

        // setup the container name used by the provider (like bucket in S3)
        String containerName = "test-container";

        // create a container in the default location
        BlobStore blobStore = context.getBlobStore();
        blobStore.createContainerInLocation(null, containerName);

        for (int i = 0; i < vals.length; i++) {

            // add blob
            BlobBuilder blobbuilder = blobStore.blobBuilder(new StringBuilder("test").append(i).toString());
            Blob blob = blobbuilder.build();
            blob.setPayload(vals[i]);
            blobStore.putBlob(containerName, blob);
        }
        context.close();

        context = new BlobStoreContextFactory().createContext("filesystem", properties);
        blobStore = context.getBlobStore();

        for (int i = 0; i < vals.length; i++) {
            Blob blobRetrieved =
                blobStore.getBlob(containerName, new StringBuilder("test").append(i).toString());
            InputStream in = blobRetrieved.getPayload().getInput();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteStreams.copy(in, out);
            if (!Arrays.equals(out.toByteArray(), vals[i])) {
                throw new IllegalStateException();
            } else {
                System.out.println("Checked array offset " + i);
            }

        }
        //
        // // close context
        // context.close();

    }
}
//
