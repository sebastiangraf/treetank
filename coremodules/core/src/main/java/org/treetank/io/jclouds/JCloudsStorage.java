/**
 * 
 */
package org.treetank.io.jclouds;

import java.util.Properties;

import org.treetank.api.INodeFactory;
import org.treetank.exception.TTException;
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
    private Properties mProperties;

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TTException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TTException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TTException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

}
