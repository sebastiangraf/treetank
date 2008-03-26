/*
 * Copyright (c) 2007, Marc Kramis
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

package org.treetank.axislayer;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NameAxisTest</h1>
 * 
 * <p>
 * Match local part of ELEMENT or ATTRIBUTE by key.
 * </p>
 */
public class NameFilter extends AbstractFilter implements IFilter {

  /** Key of name to test. */
  private final int mLocalPartKey;

  /**
   * Default constructor.
   * 
   * @param rtx Transaction this filter is bound to.
   * @param localPart Local part to check.
   */
  public NameFilter(final IReadTransaction rtx, final String localPart) {
    super(rtx);
    mLocalPartKey = rtx.keyForName(localPart);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
    return ((getTransaction().isElementKind() 
        || getTransaction().isAttributeKind()) && (getTransaction()
        .getNameKey() == mLocalPartKey));
  }
}
