/**
 * 
 */
package org.treetank.page.interfaces;

/**
 * One page representing unique pages within one revision.
 * Normally this should not occur since pages can be represented by multiple versions in the page tree.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IRevisionPage extends IPage {

    /**
     * Getting the revision of the page.
     * 
     * @return the revision of this page.
     */
    long getRevision();

}
