/**
 * 
 */
package org.treetank.page.interfaces;


/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IReferencePage extends IPage {

    long[] getReferenceKeys();

    void setReferenceKey(int pIndex, long pKey);

}
