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
public interface IPageWriteTrx extends IPageReadTrx {
    UberPage getUberPage();

    long getMaxNodeKey();

    <T extends INode> T createNode(T pnode) throws TTIOException;

}
