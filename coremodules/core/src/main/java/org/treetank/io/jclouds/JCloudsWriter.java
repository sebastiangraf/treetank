/**
 * 
 */
package org.treetank.io.jclouds;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.IPage;
import org.treetank.page.PageFactory;
import org.treetank.page.PageReference;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsWriter implements IBackendWriter {

    /** Delegate for reader. */
    private final JCloudsReader mReader;

    /** Key of nodepage. */
    private long mNodepagekey;

    public JCloudsWriter(BlobStore pBlobStore, PageFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mReader = new JCloudsReader(pBlobStore, pFac, pByteHandler, pResourceName);
        mNodepagekey = getLastNodePage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference readFirstReference() throws TTIOException, TTByteHandleException {
        return mReader.readFirstReference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(long pKey) throws TTIOException, TTByteHandleException {
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long write(PageReference pageReference) throws TTIOException, TTByteHandleException {
        // TODO make this better
        mNodepagekey++;
        final IPage page = pageReference.getPage();
        final byte[] rawPage = page.getByteRepresentation();
        final byte[] decryptedPage = mReader.mByteHandler.serialize(rawPage);
        BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(mNodepagekey));
        Blob blob = blobbuilder.build();
        blob.setPayload(decryptedPage);
        mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
        pageReference.setKey(mNodepagekey);
        return mNodepagekey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFirstReference(PageReference pageReference) throws TTIOException, TTByteHandleException {
        write(pageReference);
        try {
            BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(-1L));
            Blob blob = blobbuilder.build();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            dataOut.writeLong(pageReference.getKey());
            blob.setPayload(byteOut.toByteArray());
            mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        setLastNodePage(mNodepagekey);
        mReader.close();
    }

    /**
     * Getting the last nodePage from the persistent storage.
     * 
     * @throws TTIOException
     *             If can't get last Node page
     * @return the last nodepage-key
     */
    private long getLastNodePage() throws TTIOException {
        try {
            if (mReader.mBlobStore.blobExists(mReader.mResourceName, Long.toString(-2l))) {
                Blob blobRetrieved = mReader.mBlobStore.getBlob(mReader.mResourceName, Long.toString(-2l));
                InputStream in = blobRetrieved.getPayload().getInput();
                DataInputStream datain = new DataInputStream(in);
                long key = datain.readLong();
                datain.close();
                in.close();
                return key;
            } else {
                return 0;
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Setting the last nodePage to the persistent storage.
     * 
     * @param paramData
     *            key to be stored
     * @throws TTIOException
     *             If can't set last Node page
     */
    private void setLastNodePage(final Long paramData) throws TTIOException {
        try {
            BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(-2L));
            Blob blob = blobbuilder.build();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            dataOut.writeLong(paramData);
            blob.setPayload(byteOut.toByteArray());
            mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

}
