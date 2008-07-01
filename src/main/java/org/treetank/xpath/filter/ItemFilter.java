package org.treetank.xpath.filter;

import org.treetank.api.IFilter;
import org.treetank.axislayer.AbstractFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>ItemFilter</h1>
 * 
 * <p>
 * Match any item type (nodes and atomic values).
 * </p>
 */
public class ItemFilter extends AbstractFilter implements IFilter {

  /**
   * Default constructor.
   * 
   * @param rtx Transaction this filter is bound to.
   */
  public ItemFilter(final IReadTransaction rtx) {
    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
    //everything that is hold by an transaction is either a node or an 
    //atomic value, so this yields true for all item kinds
    return true;
  }

  
}
