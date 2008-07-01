
package org.treetank.xpath;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;

/**
 * <h1>XPath Axis</h1>
 * <p>
 * Evaluates a given XPath query.
 * </p>
 * <p>
 * Axis to iterate over the items (more precisely the item keys) of the query's
 * result sequence. <code>XPathAxis</code> extends treetanks
 * <code>IAxis</code> that extends the well-known Java
 * <code>Iterator&lt;Long&gt;</code> and <code>Iterable&lt;Long&gt;</code>
 * interfaces.
 * </p>
 * <h2>User Example</h2>
 * <p>
 * In order to use it, at first a treetank session has to be bound to the XML
 * document in question or an tnk file and a <code>ReadTransaction</code> with 
 * an <code>IItemList</code> as argument has to be started on it. 
 * (For more information how to do that, see the treetank documentation.)
 * Then the <code>XPathAxis</code> can be used like this:
 * <p>
 * 
 * <pre>
 *   ...
 *   IReadTransaction rtx = session.beginReadTransaction(new ItemList());
 *   
 *   final String query = 
 *   &quot;for $a in /articles/article[@name = \&quot;book\&quot;] return $a/price&quot;;
 *   
 *   final IAxis axis = new XPathAxis(rtx, query);
 *   while (axis.hasNext()) {
 *     // Move transaction cursor to do something.
 *     axis.next();
 *     System.out.println(rtx.getValueAsInt()););
 *   }
 *   ...
 * </pre>
 *  
 *  <pre>
 *   ...
 *   for (final long key : new XPathAxis(rtx, query)) {
 *      ...
 *   }
 *   ...
 * </pre>
 * 
 * </p>
 * 
 * @author Tina Scherer
 */
public class XPathAxis extends AbstractAxis implements IAxis {

  /** Axis holding the consecutive query execution plans of the query. */
  private IAxis pipeline;

  /**
   * <p>
   * Constructor initializing internal state.
   * </p>
   * <p>
   * Starts the query scanning and parsing and retrieves the builded query
   * execution plan from the parser.
   * </p>
   * 
   * @param rtx
   *          Transaction to operate with.
   * @param query
   *          XPath query to process.
   */
  public XPathAxis(final IReadTransaction rtx, final String query) {

    super(rtx);

    // start parsing and get execution plans
    final XPathParser parser = new XPathParser(getTransaction(), query);
    parser.parseQuery();
    pipeline = parser.getQueryPipeline();

  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {

    resetToLastKey();

    if (pipeline.hasNext()) {
      return true;
    } else {

      resetToStartKey();
      return false;
    }

  }

}
