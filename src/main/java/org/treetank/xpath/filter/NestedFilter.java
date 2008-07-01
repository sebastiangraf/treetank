
package org.treetank.xpath.filter;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractFilter;

/**
 * <h1>NestedFilter</h1>
 * <p>
 * Nests two or more IFilters.
 * </p>
 * 
 * @author Tina Scherer
 */
public class NestedFilter extends AbstractFilter implements IFilter {


  /** Tests to apply. */
  private final IFilter[] mFilter;

  /**
   * Default constructor.
   * 
   * @param rtx
   *          Transaction this filter is bound to.
   * @param axisTest
   *          Test to perform for each node found with axis.
   */
  public NestedFilter(final IReadTransaction rtx, final IFilter... axisTest) {

    super(rtx);
    mFilter = axisTest;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {

    boolean filterResult = true;

    for (final IFilter filter : mFilter) {
      filterResult = filterResult && filter.filter();
    }

    return filterResult;
  }
}
