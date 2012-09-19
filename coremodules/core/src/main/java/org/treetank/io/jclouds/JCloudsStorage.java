/**
 * 
 */
package org.treetank.io.jclouds;

import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTException;
import org.treetank.io.IConstants;
import org.treetank.io.IReader;
import org.treetank.io.IStorage;
import org.treetank.io.IWriter;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorage implements IStorage {

    /** Factory for Pages. */
    private final PageFactory mFac;

    /** Handling the byte-representation before serialization. */
    private final IByteHandlerPipeline mByteHandler;

    /** Properties of storage. */
    private final Properties mProperties;

    /** Context for the BlobStore. */
    private final BlobStoreContext mContext;

    /** BlobStore for Cloud Binding. */
    private final BlobStore mBlobStore;

    /**
     * Constructor.
     * 
     * @param pProperties
     *            not only the location of the database
     * @param pNodeFac
     *            factory for the nodes
     * @param pByteHandler
     *            handling any bytes
     * 
     */
    @Inject
    public JCloudsStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IByteHandlerPipeline pByteHandler) {
        mProperties = pProperties;
        mFac = new PageFactory(pNodeFac);
        mByteHandler = (ByteHandlerPipeline)pByteHandler;
        mContext =
            ContextBuilder.newBuilder(mProperties.getProperty(IConstants.JCLOUDSTYPE)).overrides(mProperties)
                .buildView(BlobStoreContext.class);

        mBlobStore = mContext.getBlobStore();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TTException {
        // setup the container name used by the provider (like bucket in S3)
        String containerName = mProperties.getProperty(IConstants.RESOURCE);
        if (!mBlobStore.containerExists(containerName)) {
            mBlobStore.createContainerInLocation(null, containerName);
        }
        return new JCloudsWriter(mBlobStore, mFac, mByteHandler, mProperties.getProperty(IConstants.RESOURCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TTException {
        // setup the container name used by the provider (like bucket in S3)
        String containerName = mProperties.getProperty(IConstants.RESOURCE);
        if (!mBlobStore.containerExists(containerName)) {
            mBlobStore.createContainerInLocation(null, containerName);
        }
        return new JCloudsReader(mBlobStore, mFac, mByteHandler, mProperties.getProperty(IConstants.RESOURCE));
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
    public boolean exists() throws TTException {
        if (mBlobStore.containerExists(mProperties.getProperty(IConstants.RESOURCE))
            && mBlobStore.blobExists(mProperties.getProperty(IConstants.RESOURCE), Long.toString(-2l))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

    @Override
    public void truncate() throws TTException {
        mBlobStore.deleteContainer(mProperties.getProperty(IConstants.RESOURCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JCloudsStorage [mFac=");
        builder.append(mFac);
        builder.append(", mByteHandler=");
        builder.append(mByteHandler);
        builder.append(", mProperties=");
        builder.append(mProperties);
        builder.append("]");
        return builder.toString();
    }

}
