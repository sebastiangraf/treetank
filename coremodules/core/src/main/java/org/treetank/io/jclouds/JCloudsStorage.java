/**
 * 
 */
package org.treetank.io.jclouds;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorage implements IBackend {

    /** Factory for Pages. */
    private final PageFactory mFac;

    /** Handling the byte-representation before serialization. */
    private final IByteHandlerPipeline mByteHandler;

    /** Properties of storage. */
    private final Properties mProperties;

    /** Context for the BlobStore. */
    private final BlobStoreContext mContext;

    /** BlobStore for Cloud Binding. */
    private final AsyncBlobStore mBlobStore;

    /**
     * Constructor.
     * 
     * @param pProperties
     *            not only the location of the database
     * @param pNodeFac
     *            factory for the nodes
     * @param pMetaFac
     *            factory for meta page
     * @param pByteHandler
     *            handling any bytes
     * 
     */
    @Inject
    public JCloudsStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IMetaEntryFactory pMetaFac, IByteHandlerPipeline pByteHandler) {
        mProperties = pProperties;
        mFac = new PageFactory(pNodeFac, pMetaFac);
        mByteHandler = (ByteHandlerPipeline)pByteHandler;

        mContext =
            ContextBuilder.newBuilder(mProperties.getProperty(ConstructorProps.JCLOUDSTYPE)).overrides(
                mProperties).buildView(BlobStoreContext.class);
        mBlobStore = mContext.getAsyncBlobStore();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendWriter getWriter() throws TTException {
        try {// setup the container name used by the provider (like bucket in S3)
            String containerName = mProperties.getProperty(ConstructorProps.RESOURCE);
            if (!mBlobStore.containerExists(containerName).get()) {
                mBlobStore.createContainerInLocation(null, containerName).get();
            }
            return new JCloudsWriter(mBlobStore, mFac, mByteHandler, mProperties
                .getProperty(ConstructorProps.RESOURCE));
        } catch (final ExecutionException | InterruptedException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendReader getReader() throws TTException {
        // setup the container name used by the provider (like bucket in S3)
        return new JCloudsReader(mBlobStore, mFac, mByteHandler, mProperties
            .getProperty(ConstructorProps.RESOURCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTException {
        mContext.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

    @Override
    public boolean truncate() throws TTException {
        try {
            boolean returnVal = false;
            if (mBlobStore.containerExists(mProperties.getProperty(ConstructorProps.RESOURCE)).get()) {
                mBlobStore.deleteContainer(mProperties.getProperty(ConstructorProps.RESOURCE)).get();
                returnVal = true;
            }
            mContext.close();
            return returnVal;
        } catch (final ExecutionException | InterruptedException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public void initialize() throws TTIOException {
        // not needed over here
    }
}
