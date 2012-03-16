/**
 * 
 */
package org.treetank.api;

import org.treetank.page.UberPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IPageWriteTransaction extends IPageReadTransaction {
    UberPage getUberPage();
}
