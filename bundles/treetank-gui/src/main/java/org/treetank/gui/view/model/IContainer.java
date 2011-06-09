/**
 * 
 */
package org.treetank.gui.view.model;

import org.treetank.gui.view.sunburst.EPruning;

/**
 * Container used as parameters for models.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @param <T>
 *            the concrete instance
 * 
 */
public interface IContainer<T> {

    /**
     * Set start key.
     * 
     * @param paramKey
     *            node key to start from
     * @return instance
     */
    T setStartKey(final long paramKey);

    /**
     * Determines if tree should be pruned or not.
     * 
     * @param paramPruning
     *            {@link EPruning} enum which determines if tree should be pruned or not
     * @return instance
     */
    T setPruning(final EPruning paramPruning);

    /**
     * Set revision to compare.
     * 
     * @param paramRevision
     *            the Revision to set
     * @return this
     */

}
