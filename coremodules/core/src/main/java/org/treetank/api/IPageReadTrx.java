/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.RevisionRootPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageReadTrx {

    INode getNode(final long pKey) throws TTException;

    RevisionRootPage getActualRevisionRootPage() throws TTIOException;

    String getName(final int pKey);

    byte[] getRawName(final int pKey);

    void close() throws TTIOException;

    boolean isClosed();

}
