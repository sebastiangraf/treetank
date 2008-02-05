package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NodeAxisTest</h1>
 * 
 * <p>
 * Only match process instruction nodes.
 * </p>
 */
  public class PIFilter extends AbstractFilter implements IFilter {

    /**
     * Default constructor.
     * 
     * @param rtx Transaction this filter is bound to.
     */
    public PIFilter(final IReadTransaction rtx) {
      super(rtx);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean filter() {
      throw new IllegalStateException("PI filter is not implemented yet");
     
      //return (getTransaction().isPIKind());
    }

  }
