/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.treetank.access.conf;

import static com.google.common.base.Objects.toStringHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.treetank.access.Session;
import org.treetank.api.IDataFactory;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.revisioning.IRevisioning;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * <h1>ResourceConfiguration</h1>
 * 
 * <p>
 * Holds the settings for a resource which acts as a base for session that can not change. This includes all
 * settings which are persistent. Each {@link ResourceConfiguration} is furthermore bound to one fixed
 * database denoted by a related {@link StorageConfiguration}.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class ResourceConfiguration {

    /**
     * Paths for a {@link Session}. Each resource has the same folder.layout.
     */
    public enum Paths implements IConfigurationPath {

        /** Folder for storage of data. */
        Data(new File("data"), true),
        /** Folder for transaction log. */
        TransactionLog(new File("log"), true),
        /** File to store the resource settings. */
        ConfigBinary(new File("ressetting.obj"), false);

        /** Location of the file. */
        private final File mFile;

        /** Is the location a folder or no? */
        private final boolean mIsFolder;

        /**
         * Constructor.
         * 
         * @param pFile
         *            to be set
         * @param pIsFolder
         *            to be set.
         */
        private Paths(final File pFile, final boolean pIsFolder) {
            this.mFile = pFile;
            this.mIsFolder = pIsFolder;
        }

        /**
         * Getting the file for the kind.
         * 
         * @return the file to the kind
         */
        public File getFile() {
            return mFile;
        }

        /**
         * Check if file is denoted as folder or not.
         * 
         * @return boolean if file is folder
         */
        public boolean isFolder() {
            return mIsFolder;
        }

    }

    // MEMBERS FOR FIXED FIELDS
    /** Type of Storage (File, Berkeley). */
    public final IBackend mBackend;

    /** Kind of revisioning (Incremental, Differential). */
    public final IRevisioning mRevision;

    /** Path for the resource to be associated. */
    public final Properties mProperties;

    /** Data Factory for deserializing data. */
    public final IDataFactory mDataFac;

    /** MetaEntry Factory for deserializing meta-entries. */
    public final IMetaEntryFactory mMetaFac;

    // END MEMBERS FOR FIXED FIELDS

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param pProperties
     * @param pBackend
     * @param pRevision
     * @param pDataFac
     */
    @Inject
    public ResourceConfiguration(@Assisted Properties pProperties, IBackendFactory pBackend,
        IRevisioning pRevision, IDataFactory pDataFac, IMetaEntryFactory pMetaFac) {

        this(pProperties, pBackend.create(pProperties), pRevision, pDataFac, pMetaFac);
    }

    /**
     * Constructor.
     * 
     * @param pProperties
     * @param pStorage
     * @param pRevisioning
     * @param pDataFac
     * @param pMetaFac
     */
    public ResourceConfiguration(Properties pProperties, IBackend pStorage, IRevisioning pRevisioning,
        IDataFactory pDataFac, IMetaEntryFactory pMetaFac) {
        mProperties = pProperties;
        mBackend = pStorage;
        mRevision = pRevisioning;
        mDataFac = pDataFac;
        mMetaFac = pMetaFac;
    }

    /**
     * 
     * Factory for generating an {@link ResourceConfiguration}-instance. Needed mainly
     * because of Guice-Assisted utilization.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static interface IResourceConfigurationFactory {

        /**
         * Generating a storage for a fixed file.
         * 
         * 
         * @param pProperties
         *            Properties of resource to be set.
         * @return an {@link ResourceConfiguration}-instance
         */
        ResourceConfiguration create(Properties pProperties);
    }

    private static final String[] JSONNAMES = {
        "metaentryClass", "revisioningClass", "DataFactoryClass", "byteHandlerClasses", "storageClass",
        "properties"
    };

    public static void serialize(final ResourceConfiguration pConfig) throws TTIOException {
        try {

            final File file =
                new File(new File(new File(pConfig.mProperties.getProperty(ConstructorProps.STORAGEPATH),
                    StorageConfiguration.Paths.Data.getFile().getName()), pConfig.mProperties
                    .getProperty(ConstructorProps.RESOURCE)), Paths.ConfigBinary.getFile().getName());

            FileWriter fileWriter = new FileWriter(file);
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            jsonWriter.beginObject();
            // caring about the meta-entries
            jsonWriter.name(JSONNAMES[0]).value(pConfig.mMetaFac.getClass().getName());
            // caring about the versioning
            jsonWriter.name(JSONNAMES[1]).value(pConfig.mRevision.getClass().getName());
            // caring about the DataFactory
            jsonWriter.name(JSONNAMES[2]).value(pConfig.mDataFac.getClass().getName());
            // caring about the ByteHandlers
            IByteHandlerPipeline byteHandler = pConfig.mBackend.getByteHandler();
            jsonWriter.name(JSONNAMES[3]);
            jsonWriter.beginArray();
            for (IByteHandler handler : byteHandler) {
                jsonWriter.value(handler.getClass().getName());
            }
            jsonWriter.endArray();
            // caring about the storage
            jsonWriter.name(JSONNAMES[4]).value(pConfig.mBackend.getClass().getName());
            jsonWriter.name(JSONNAMES[5]);
            jsonWriter.beginObject();
            for (String key : pConfig.mProperties.stringPropertyNames()) {
                jsonWriter.name(key).value(pConfig.mProperties.getProperty(key));
            }
            jsonWriter.endObject();
            jsonWriter.endObject();
            jsonWriter.close();
            fileWriter.close();
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Deserializing a Resourceconfiguration out of a JSON-file from the persistent storage.
     * The order is important and the reader is passed through the objects as visitor.
     * 
     * @param pFile
     *            where the resource lies in.
     * @return a complete {@link ResourceConfiguration} instance.
     * @throws TTIOException
     */
    public static ResourceConfiguration deserialize(final File pFile, final String pResource)
        throws TTIOException {
        try {

            final File file =
                new File(new File(new File(pFile, StorageConfiguration.Paths.Data.getFile().getName()),
                    pResource), Paths.ConfigBinary.getFile().getName());

            FileReader fileReader = new FileReader(file);
            JsonReader jsonReader = new JsonReader(fileReader);
            jsonReader.beginObject();
            // caring about the kind of the metabucket
            jsonReader.nextName().equals(JSONNAMES[0]);
            Class<?> metaBucketClazz = Class.forName(jsonReader.nextString());
            // caring about the versioning
            jsonReader.nextName().equals(JSONNAMES[1]);
            Class<?> revClazz = Class.forName(jsonReader.nextString());
            // caring about the DataFactory
            jsonReader.nextName().equals(JSONNAMES[2]);
            Class<?> dataFacClazz = Class.forName(jsonReader.nextString());

            // caring about the ByteHandlers
            List<IByteHandler> handlerList = new ArrayList<IByteHandler>();
            if (jsonReader.nextName().equals(JSONNAMES[3])) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    Class<?> handlerClazz = Class.forName(jsonReader.nextString());
                    Constructor<?> handlerCons = handlerClazz.getConstructors()[0];
                    if (handlerClazz.getName().equals(Encryptor.class.getName())) {
                        handlerList.add((IByteHandler)handlerCons.newInstance(StandardSettings.KEY));
                    } else {
                        handlerList.add((IByteHandler)handlerCons.newInstance());
                    }

                }
                jsonReader.endArray();
            }
            ByteHandlerPipeline pipeline =
                new ByteHandlerPipeline(handlerList.toArray(new IByteHandler[handlerList.size()]));
            // caring about the storage
            jsonReader.nextName().equals(JSONNAMES[4]);
            Class<?> storageClazz = Class.forName(jsonReader.nextString());
            jsonReader.nextName().equals(JSONNAMES[5]);
            Properties props = new Properties();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                props.setProperty(jsonReader.nextName(), jsonReader.nextString());
            }
            jsonReader.endObject();
            jsonReader.endObject();
            jsonReader.close();
            fileReader.close();

            Constructor<?> metaBucketCons = metaBucketClazz.getConstructors()[0];
            IMetaEntryFactory metaFac = (IMetaEntryFactory)metaBucketCons.newInstance();

            Constructor<?> dataFacCons = dataFacClazz.getConstructors()[0];
            IDataFactory dataFactory = (IDataFactory)dataFacCons.newInstance();

            Constructor<?> revCons = revClazz.getConstructors()[0];
            IRevisioning revObject = (IRevisioning)revCons.newInstance();

            Constructor<?> storageCons = storageClazz.getConstructors()[0];
            IBackend backend = (IBackend)storageCons.newInstance(props, dataFactory, metaFac, pipeline);

            return new ResourceConfiguration(props, backend, revObject, dataFactory, metaFac);

        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException
        | InvocationTargetException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mBackend, mRevision, mProperties, mDataFac);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object pObj) {
        return this.hashCode() == pObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mBackend", mBackend.getClass()).add("mRevision", mRevision).add(
            "mProperties", mProperties).add("mDataFac", mDataFac.getClass().getName()).toString();
    }
}
