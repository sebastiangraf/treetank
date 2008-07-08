/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xpath.functions;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.axislayer.FilterAxis;
import org.treetank.axislayer.TextFilter;
import org.treetank.utils.TypedValue;

/**
 * <h1>FNString</h1>
 * <p>
 * IAxis that represents the function fn:count specified in <a
 * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0
 * Functions and Operators</a>.
 * </p>
 * <p>
 * The function returns the string value of the current node or the argument
 * nodes.
 * </p>
 */
public class FNString extends AbstractFunction {

  /**
   * Constructor. Initializes internal state and do a statical analysis
   * concerning the function's arguments.
   * 
   * @param rtx
   *          Transaction to operate on
   * @param args
   *          List of function arguments
   * @param min
   *          min number of allowed function arguments
   * @param max
   *          max number of allowed function arguments
   * @param returnType
   *          the type that the function's result will have
   */
  public FNString(final IReadTransaction rtx, final List<IAxis> args,
      final int min, final int max, final int returnType) {

    super(rtx, args, min, max, returnType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected byte[] computeResult() {

    String value;

    if (getArgs().size() == 0) {
      value = getStrValue();
    } else {
      IAxis axis = getArgs().get(0);
      StringBuilder val = new StringBuilder();
      while (axis.hasNext()) {
        String nodeValue = getStrValue();
        if (!nodeValue.equals("")) {
          if (val.length() > 0) {
            val.append(" ");
          }
          val.append(nodeValue);
        }
      }
      value = val.toString();
    }

    return TypedValue.getBytes(value);

  }

  /**
   * Returns the string value of an item. If the item is the empty sequence, the
   * zero-length string is returned. If the item is a node, the function returns
   * the string-value of the node, as obtained using the dm:string-value
   * accessor defined in the <a
   * href="http://www.w3.org/TR/xpath-datamodel/#dm-string-value">Section 5.13
   * string-value AccessorDM</a>. If the item is an atomic value, then the
   * function returns the same string as is returned by the expression " $arg
   * cast as xs:string " (see 17 Casting).
   * 
   * @return the context item's string value.
   */
  private String getStrValue() {

    StringBuilder value = new StringBuilder();

    if (getTransaction().getNodeKey() >= 0) { // is node
      if (getTransaction().isAttributeKind() || getTransaction().isTextKind()) {
        value.append(getTransaction().getValue());
      } else if (getTransaction().isDocumentRootKind()
          || getTransaction().isElementKind()) {
        IAxis axis = new FilterAxis(new DescendantAxis(getTransaction()),
            new TextFilter(getTransaction()));
        while (axis.hasNext()) {
          if (value.length() > 0) {
            value.append(" ");
          }
          value.append(getTransaction().getValue());

        }

      } else {
        throw new IllegalStateException();
      }

    } else {
      value.append(getTransaction().getValue());
    }

    return value.toString();
  }

}
