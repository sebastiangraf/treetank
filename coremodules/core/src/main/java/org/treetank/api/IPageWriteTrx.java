/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;

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
     * Getting most recent node key and incrementing it.
     * 
     * @return the most recent node key.
     */
    long incrementNodeKey();

    /**
     * Setting a node and storing the node in the page-layer.
     * 
     * @param pnode
     *            the node to be stored.
     * @throws TTException
     *             if anything weird happens.
     */
    long setNode(INode pnode) throws TTException;

    /**
     * Removing the node from the storage.
     * 
     * @param pNode
     *            to be removed
     * @throws TTException
     */
    void removeNode(final INode pNode) throws TTException;

    /**
     * Simple commit of this page transaction to store the newest version.
     * 
     * @return true of successful, false otherwise
     * @throws if
     *             anything weird happens
     */
    void commit() throws TTException;


}
