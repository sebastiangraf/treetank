/**
 * 
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 * 
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.treetank.io.jclouds;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.contains;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Module;

/**
 * Demonstrates the use of {@link BlobStore}.
 * 
 * Usage is: java MainApp \"provider\" \"identity\" \"credential\" \"containerName\"
 * 
 * @author Carlos Fernandes
 * @author Adrian Cole
 */
public class MainApp {

    private static byte[][] vals = new byte[128][128];

    static {
        Random ran = new Random();
        for (int i = 0; i < vals.length; i++) {
            ran.nextBytes(vals[i]);
        }
    }

    public static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(Apis
        .viewableAs(BlobStoreContext.class), Apis.idFunction());

    public static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(Providers
        .viewableAs(BlobStoreContext.class), Providers.idFunction());

    public static final Set<String> allKeys = ImmutableSet.copyOf(Iterables.concat(appProviders.keySet(),
        allApis.keySet()));

    public static int PARAMETERS = 4;
    public static String INVALID_SYNTAX =
        "Invalid number of parameters. Syntax is: \"provider\" \"identity\" \"credential\" \"containerName\" ";

    public static void main(String[] args) throws IOException {

        if (args.length < PARAMETERS)
            throw new IllegalArgumentException(INVALID_SYNTAX);

        // Args

        String provider = args[0];

        // note that you can check if a provider is present ahead of time
        checkArgument(contains(allKeys, provider), "provider %s not in supported list: %s", provider, allKeys);

        String identity = args[1];
        String credential = args[2];
        String containerName = args[3];

        Properties mProperties = new Properties();
        mProperties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, "/tmp/bla");

        // Init
        BlobStoreContext context =
            ContextBuilder.newBuilder(provider).credentials(identity, credential)
                .overrides(mProperties)

                // default jclouds has few dependencies, and uses builtin logging, date, and encryption.
                // we can add support for libraries by adding driver modules like below
                .modules(
                    ImmutableSet.<Module> of(new EnterpriseConfigurationModule(), new SLF4JLoggingModule()))
                .buildView(BlobStoreContext.class);

        // Create Container
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

        BlobStoreContext context2 =
            ContextBuilder.newBuilder(provider).credentials(identity, credential)
                .overrides(mProperties)

                // default jclouds has few dependencies, and uses builtin logging, date, and encryption.
                // we can add support for libraries by adding driver modules like below
                .modules(
                    ImmutableSet.<Module> of(new EnterpriseConfigurationModule(), new SLF4JLoggingModule()))
                .buildView(BlobStoreContext.class);
        blobStore = context2.getBlobStore();

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

        // close context
        context2.close();

    }
}
