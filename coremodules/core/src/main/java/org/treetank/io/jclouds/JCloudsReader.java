/**
 * 
 */
package org.treetank.io.jclouds;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsReader implements IBackendReader {

    /** Blob Store for Reading the data. */
    protected final AsyncBlobStore mBlobStore;

    /** Factory for building Pages. */
    private final PageFactory mFac;

    /** Inflater to decompress. */
    protected final IByteHandlerPipeline mByteHandler;

    /** Resource name of this container and the database. */
    protected final String mResourceName;

    /** Cache for reading data. */
    protected final Cache<Long, IPage> mCache;

    public JCloudsReader(AsyncBlobStore pBlobStore, PageFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mBlobStore = pBlobStore;
        mByteHandler = pByteHandler;
        mFac = pFac;
        mResourceName = pResourceName;
        mCache = CacheBuilder.newBuilder().maximumSize(10000).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberPage readUber() throws TTIOException {
        try {
            Blob blobRetrieved = mBlobStore.getBlob(mResourceName, Long.toString(-1l)).get();
            DataInputStream datain = new DataInputStream(blobRetrieved.getPayload().getInput());
            long uberpagekey = datain.readLong();
            final UberPage page = (UberPage)read(uberpagekey);
            datain.close();
            return page;
        } catch (final IOException | ExecutionException | InterruptedException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws
     */
    @Override
    public IPage read(long pKey) throws TTIOException {
        IPage returnval = mCache.getIfPresent(pKey);
        if (returnval == null) {
            try {
                Blob blobRetrieved = mBlobStore.getBlob(mResourceName, Long.toString(pKey)).get();
                DataInputStream datain =
                    new DataInputStream(mByteHandler.deserialize(blobRetrieved.getPayload().getInput()));
                returnval = mFac.deserializePage(datain);
                datain.close();
                mCache.put(pKey, returnval);
            } catch (IOException | TTByteHandleException | ExecutionException | InterruptedException exc) {
                throw new TTIOException(exc);
            }
        }
        return returnval;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mCache.invalidateAll();
    }
}
