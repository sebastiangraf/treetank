/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;
import org.treetank.page.UberPage;

/**
 * Write-Transaction of a page ensuring read- and write access to any pages.
 * The transaction is bound on the very last revision and bases on a session.
 * 
 * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IPageReadTrx pRtx = session.beginWriteReadTransaction();
 * </code>
 * 
 * Each {@link IPageWriteTrx} can afterwards get nodes from the page-layer and the underlaying backend as well
 * as store new {@link INode}s in the backend.
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageWriteTrx extends IPageReadTrx {

    /**
     * Getting the actual {@link UberPage}.
     * 
     * @return the actual {@link UberPage} for getting additional information.
     */
    UberPage getUberPage();

    /**
     * Getting most recent node key.
     * 
     * @return the most recent node key.
     */
    long getMaxNodeKey();

    /**
     * Creating a new node and storing the node in the page-layer. This method invokes the
     * {@link INode#setNodeKey(long)}.
     * 
     * @param pnode
     *            the node to be stored.
     * @return the same node, with node key
     * @throws TTException
     *             if anything weird happens.
     */
    <T extends INode> T createNode(T pnode) throws TTException;

    /**
     * Simple commit of this page transaction to store the newest version.
     * 
     * @return the new UberPage of this revision
     * @throws if
     *             anything weird happens
     */
    UberPage commit() throws TTException;

}
