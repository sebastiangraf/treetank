/**
 * 
 */
package org.treetank.api;

import org.treetank.exception.TTException;
import org.treetank.page.UberPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageWriteTrx extends IPageReadTrx {
    UberPage getUberPage();

    long getMaxNodeKey();

    <T extends INode> T createNode(T pnode) throws TTException;

}
