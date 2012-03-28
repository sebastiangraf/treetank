/**
 * 
 */
package org.treetank.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.jclouds.atmos.AtmosAsyncClient;
import org.jclouds.atmos.AtmosClient;
import org.jclouds.azureblob.AzureBlobAsyncClient;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.openstack.swift.SwiftAsyncClient;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.rest.RestContext;
import org.jclouds.s3.S3AsyncClient;
import org.jclouds.s3.S3Client;

public class MainApp {

    public static void main(String[] args) throws IOException {

        // setup where the provider must store the files
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, "/tmp/filesystemstorage");
        // setup the container name used by the provider (like bucket in S3)
        String containerName = "test-container";

        // get a context with filesystem that offers the portable BlobStore api
        BlobStoreContext context = new BlobStoreContextFactory().createContext("filesystem", properties);

        // create a container in the default location
        BlobStore blobStore = context.getBlobStore();
        blobStore.createContainerInLocation(null, containerName);

        // add blob
//        Blob blob = blobStore.newBlob("test");
//        blob.setPayload("test data");
//        blobStore.putBlob(containerName, blob);
//
//        // retrieve blob
//        Blob blobRetrieved = blobStore.getBlob(containerName, "test");
//
//        // delete blob
//        blobStore.removeBlob(containerName, "test");

        // close context
        context.close();

    }
}
