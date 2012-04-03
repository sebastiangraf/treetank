/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTIOException;
import org.treetank.node.interfaces.INode;
import org.treetank.page.UberPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageWriteTransaction extends IPageReadTransaction {
    UberPage getUberPage();

    long getMaxNodeKey();

    INode createNode(INode pnode) throws TTIOException;

}
