/**
 * 
 */
package org.treetank.io.jclouds;

import org.treetank.exception.TTException;
import org.treetank.io.IReader;
import org.treetank.io.IStorage;
import org.treetank.io.IWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorage implements IStorage {

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
        // TODO Auto-generated method stub
        return null;
    }

}
