/**
 * 
 */
package org.treetank.io.jclouds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsReader implements IBackendReader {

    /** Blob Store for Reading the data. */
    protected final BlobStore mBlobStore;

    /** Factory for building Pages. */
    private final PageFactory mFac;

    /** Inflater to decompress. */
    protected final IByteHandlerPipeline mByteHandler;

    /** Resource name of this container and the database. */
    protected final String mResourceName;

    public JCloudsReader(BlobStore pBlobStore, PageFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mBlobStore = pBlobStore;
        mByteHandler = pByteHandler;
        mFac = pFac;
        mResourceName = pResourceName;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws
     */
    @Override
    public IPage read(long pKey) throws TTIOException {
        try {
            Blob blobRetrieved = mBlobStore.getBlob(mResourceName, Long.toString(pKey));
            InputStream in = blobRetrieved.getPayload().getInput();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteStreams.copy(in, out);
            byte[] decryptedPage = mByteHandler.deserialize(out.toByteArray());
            out.close();
            in.close();
            return mFac.deserializePage(decryptedPage);
        } catch (final IOException | TTByteHandleException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
    }

}
