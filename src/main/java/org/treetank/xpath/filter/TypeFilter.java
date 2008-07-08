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

package org.treetank.xpath.filter;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractFilter;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>TypeFilter</h1>
 * 
 * <p>
 * Only match nodes with the specified value type.
 * </p>
 */
public class TypeFilter extends AbstractFilter implements IFilter {

  private final int mType;

  /**
   * Constructor. Initializes the internal state.
   *
   * @param rtx Transaction this filter is bound to.
   * @param type Type to match
   */
  public TypeFilter(final IReadTransaction rtx, final int type) {
    super(rtx);
    mType = type;

    //TODO: not really good solution
    if (Type.getType(mType) == null) {
      throw new XPathError(ErrorType.XPST0051);
    }
  }

  /**
   * Constructor. Initializes the internal state.
   *
   * @param rtx Transaction this filter is bound to.
   * @param typeName Name of the type to match
   */
  public TypeFilter(final IReadTransaction rtx, final String typeName) {
    this(rtx, rtx.keyForName(typeName));

  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
    return getTransaction().getTypeKey() == mType;
  }

}
