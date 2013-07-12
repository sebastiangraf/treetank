
/**
 * 
 */
package org.treetank.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BucketCleaner {

    private static final String CONTAINER = "benchResourcegrave92838";

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        cleanBucket();

    }

//    private static void downloadBucket() throws IOException {
//        final File folder = new File("/tmp/buckets");
//        folder.mkdirs();
//
//        String[] awsCredentials = getCredentials();
//        if (awsCredentials.length == 0) {
//            System.out.println("Please set credentials in .credentials!");
//            System.exit(-1);
//        }
//
//        BlobStoreContext context =
//            ContextBuilder.newBuilder("aws-s3").credentials(awsCredentials[0], awsCredentials[1]).buildView(
//                BlobStoreContext.class);
//        BlobStore store = context.getBlobStore();
//
//        PageSet<? extends StorageMetadata> md = store.list(CONTAINER);
//        for (StorageMetadata data : md) {
//            final File concreteFile = new File(folder, data.getName());
//            Blob blob = store.getBlob(CONTAINER, data.getName());
//            byte[] value = ByteStreams.toByteArray(blob.getPayload().getInput());
//            FileOutputStream stream = new FileOutputStream(concreteFile);
//            stream.write(value);
//            stream.close();
//        }
//
//    }

    private static void cleanBucket() {
        String[] awsCredentials = getCredentials();
        if (awsCredentials.length == 0) {
            System.out.println("Please set credentials in .credentials!");
            System.exit(-1);
        }

        String[] containerName = {
            CONTAINER
        };

        BlobStoreContext context =
            ContextBuilder.newBuilder("aws-s3").credentials(awsCredentials[0], awsCredentials[1]).buildView(
                BlobStoreContext.class);
        BlobStore store = context.getBlobStore();
        for (String name : containerName) {
            if (store.containerExists(name)) {
                store.clearContainer(name);
                store.deleteContainer(name);
                System.out.println(name + " existing, removed");
            } else {
                store.createContainerInLocation(null, name);
                store.clearContainer(name);
                store.deleteContainer(name);
                System.out.println(name + " not existing, created and removed");
            }
        }
        context.close();
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
