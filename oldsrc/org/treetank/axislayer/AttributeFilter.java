package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;


/**
 * <h1>AttributeAxisTest</h1>
 * 
 * <p>
 * Only match ATTRIBUTE nodes.
 * </p>
 */
public class AttributeFilter extends AbstractFilter implements IFilter {

  /**
   * Default constructor.
   * 
   * @param rtx Transaction this filter is bound to.
   */
  public AttributeFilter(final IReadTransaction rtx) {
    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
      return (getTransaction().isAttributeKind());
    }

}

