/**
 * 
 */
package org.treetank.page.interfaces;

import org.treetank.page.PageReference;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IReferencePage extends IPage {

    PageReference[] getReferences();

    long[] getReferenceKeys();

    void setReferenceKey(int pIndex, long pKey);

}
