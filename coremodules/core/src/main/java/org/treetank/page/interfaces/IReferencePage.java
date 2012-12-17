/**
 * 
 */
package org.treetank.page.interfaces;

/**
 * Interface denoting all pages holding references to other pages.
 * The references are represented by the keys of the serialized storage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IReferencePage extends IPage {

    /**
     * Getting the keys of the referenced pages.
     * 
     * @return the keys for the referenced pages.
     */
    long[] getReferenceKeys();

    /**
     * Setting one key of a page to be referenced from this page.
     * 
     * @param pIndex
     *            offset of the key to be referenced
     * @param pKey
     *            the key of the page to be referenced
     */
    void setReferenceKey(int pIndex, long pKey);

}
