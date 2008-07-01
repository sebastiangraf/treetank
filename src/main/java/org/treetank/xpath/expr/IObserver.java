
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;

/**
 * <h1>IObserver</h1>
 * <p>
 * Interface for all axis that observe another axis' state.
 * </p>
 * 
 * @author Tina Scherer
 */
public interface IObserver extends IAxis {

  /**
   * This method is called whenever the observed axis is changed.
   * 
   * @param itemKey
   *          the item key of the current context item of the observed axis
   */
  void update(final long itemKey);

}
