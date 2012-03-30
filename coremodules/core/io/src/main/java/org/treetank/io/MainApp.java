/**
 * 
 */
package org.treetank.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.jclouds.Constants;
import org.jclouds.blobstore.BlobMap;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.filesystem.reference.FilesystemConstants;

import com.google.common.io.ByteStreams;

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
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, "/tmp/filesystemstorage");
        properties.setProperty(Constants.PROPERTY_CREDENTIAL, "test");

        // get a context with filesystem that offers the portable BlobStore api
        BlobStoreContext context = new BlobStoreContextFactory().createContext("filesystem", properties);

        BlobMap map = context.createBlobMap("container1");
        BlobBuilder builder = map.blobBuilder();
        Blob blob = builder.build();

        for (int i = 0; i < vals.length; i++) {
            blob.setPayload(vals[i]);
            map.put(new StringBuilder("key").append(i).toString(), blob);
        }
        context.close();
        context = new BlobStoreContextFactory().createContext("filesystem", properties);

        map = context.createBlobMap("container1");
        builder = map.blobBuilder();
        for (int i = 0; i < vals.length; i++) {
            blob = map.get(new StringBuilder("key").append(i).toString());
            InputStream in = blob.getPayload().getInput();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteStreams.copy(in, out);
            if (!Arrays.equals(out.toByteArray(), vals[i])) {
                throw new IllegalStateException();
            } else {
                System.out.println("Checked array offset " + i);
            }

        }

    }
}
