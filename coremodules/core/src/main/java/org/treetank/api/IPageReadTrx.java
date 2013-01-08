/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.RevisionRootPage;

/**
 * Read-Transaction of a page ensuring read-only access to any pages.
 * The transaction must be bound to a revision and bases on a session.
 * 
 * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IPageReadTrx pRtx = session.beginPageReadTransaction(REVISION);
 * </code>
 * 
 * Each {@link IPageReadTrx} can afterwards get nodes from the page-layer and the underlaying backend.
 * Note that each session furthermore has access to a centralized store of common strings referencable over a
 * key.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageReadTrx {

    /**
     * Getting the node related to a key.
     * 
     * @param pKey
     *            the key of the node
     * @return a suitable {@link INode}
     * @throws TTException
     *             if anything weird happens
     */
    INode getNode(final long pKey) throws TTIOException;

    /**
     * Getting the most related {@link RevisionRootPage} for additional content.
     * 
     * @return the related {@link RevisionRootPage}
     * @throws TTIOException
     *             if deserialization fails.
     */
    RevisionRootPage getActualRevisionRootPage() throws TTIOException;

    /**
     * Getting name to a defined key.
     * 
     * @param pKey
     *            the key where the string can be found e.g. a hash.
     * @return the string to the related key.
     */
    String getName(final int pKey);

    /**
     * Close the transaction.
     * 
     * @return true if successful, false otherwise
     * @throws TTIOException
     *             if anything weird happens
     */
    boolean close() throws TTIOException;

    /**
     * Check if transaction is closed.
     * 
     * @return true if closed, false otherwise
     */
    boolean isClosed();

}
