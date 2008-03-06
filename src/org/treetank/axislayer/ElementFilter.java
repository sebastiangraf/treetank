package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NodeAxisTest</h1>
 * 
 * <p>
 * Only match ELEMENTnodes.
 * </p>
 */
public class ElementFilter extends AbstractFilter implements IFilter {

  /**
   * Default constructor.
   * 
   * @param rtx Transaction this filter is bound to.
   */
  public ElementFilter(final IReadTransaction rtx) {
    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
    return getTransaction().isElementKind();
  }

}
