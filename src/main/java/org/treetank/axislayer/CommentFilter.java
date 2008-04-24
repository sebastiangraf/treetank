package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NodeAxisTest</h1>
 * 
 * <p>
 * Only match comment nodes.
 * </p>
 */
  public class CommentFilter extends AbstractFilter implements IFilter {

    /**
     * Default constructor.
     * 
     * @param rtx Transaction this filter is bound to.
     */
    public CommentFilter(final IReadTransaction rtx) {
      super(rtx);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean filter() {
      return getTransaction().getKind() == 8;
      
      //TODO: As soon as an comment node is implemented, use the second version, 
      //because this is much cleaner and more consistent to the other 
      //node-filters.
      //return (getTransaction().isCommentKind());
    }

  }


