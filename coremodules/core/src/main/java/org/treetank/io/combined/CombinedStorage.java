/**
 * 
 */
package org.treetank.io.combined;

import java.util.Properties;

import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.IDataFactory;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackendWriter;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Combined Storage with a BDB in the front and a JClouds in the back.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class CombinedStorage implements IBackend {

    /** Local Backend. */
    private final BerkeleyStorage mLocalBackend;

    /** Remote Backend. */
    private final JCloudsStorage mRemoteBackend;

    /**
     * Simple constructor.
     * 
     * @param pProperties
     *            not only the file associated with the database
     * @param pDataFac
     *            factory for the datas
     * @param pMetaFac
     *            factory for meta bucket
     * @param pByteHandler
     *            handling any bytes
     * @throws TTIOException
     *             of something odd happens while database-connection
     */
    @Inject
    public CombinedStorage(@Assisted Properties pProperties, IDataFactory pDataFac,
        IMetaEntryFactory pMetaFac, IByteHandlerPipeline pByteHandler) throws TTIOException {
        mLocalBackend = new BerkeleyStorage(pProperties, pDataFac, pMetaFac, pByteHandler);
        mRemoteBackend = new JCloudsStorage(pProperties, pDataFac, pMetaFac, pByteHandler.clone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendWriter getWriter() throws TTException {
        final IBackendWriter firstWriter = mLocalBackend.getWriter();
        final IBackendWriter secondWriter = mRemoteBackend.getWriter();
        return new CombinedWriter(firstWriter, secondWriter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendReader getReader() throws TTException {
        final IBackendReader firstReader = mLocalBackend.getReader();
        final IBackendReader secondReader = mRemoteBackend.getReader();
        return new CombinedReader(firstReader, secondReader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTException {
        mRemoteBackend.close();
        mLocalBackend.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mLocalBackend.getByteHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean truncate() throws TTException {
        return mLocalBackend.truncate() && mRemoteBackend.truncate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() throws TTIOException {
        mRemoteBackend.initialize();
        mLocalBackend.initialize();
    }

}
