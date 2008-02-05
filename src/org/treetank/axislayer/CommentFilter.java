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
      throw new IllegalStateException("Comment filter is not implemented yet");
     
      //return (getTransaction().isCommentKind());
    }

  }


