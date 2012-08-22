/**
 * 
 */
package org.treetank.io.jclouds;

import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IReader;
import org.treetank.page.IPage;
import org.treetank.page.PageReference;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsReader implements IReader {

    

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference readFirstReference() throws TTIOException, TTByteHandleException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(long pKey) throws TTIOException, TTByteHandleException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        // TODO Auto-generated method stub

    }

}
