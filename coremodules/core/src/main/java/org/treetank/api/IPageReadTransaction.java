/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTIOException;
import org.treetank.node.interfaces.INode;
import org.treetank.page.RevisionRootPage;
import org.treetank.utils.ItemList;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageReadTransaction {

    INode getNode(final long pKey) throws TTIOException;

    RevisionRootPage getActualRevisionRootPage() throws TTIOException;

    String getName(final int pKey);

    byte[] getRawName(final int pKey);

    ItemList getItemList();

    void close() throws TTIOException;

}
