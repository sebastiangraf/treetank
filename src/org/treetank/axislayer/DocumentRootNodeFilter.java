package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;


/**
 * <h1>NodeAxisTest</h1>
 * 
 * <p>
 * Only match ROOT nodes.
 * </p>
 */
public class DocumentRootNodeFilter extends AbstractFilter implements IFilter {

  /**
   * Default constructor.
   * 
   * @param rtx Transaction this filter is bound to.
   */
  public DocumentRootNodeFilter(final IReadTransaction rtx) {
    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
      return (getTransaction().isDocumentRootKind());
    }

}





