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
import org.treetank.utils.UTF;

/**
 * <h1>ValueAxisTest</h1>
 * 
 * <p>
 * Only match nodes of kind TEXT whoms value matches.
 * </p>
 */
public class ValueFilter extends AbstractFilter implements IFilter {

  /** Value test to do. */
  private final byte[] mValue;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Transaction to bind filter to.
   * @param value Value to find.
   */
  public ValueFilter(final IReadTransaction rtx, final byte[] value) {
    super(rtx);
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Transaction to bind filter to.
   * @param value Value to find.
   */
  public ValueFilter(final IReadTransaction rtx, final String value) {
    this(rtx, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Transaction to bind filter to.
   * @param value Value to find.
   */
  public ValueFilter(final IReadTransaction rtx, final int value) {
    this(rtx, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Transaction to bind filter to.
   * @param value Value to find.
   */
  public ValueFilter(final IReadTransaction rtx, final long value) {
    this(rtx, UTF.getBytes(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean filter() {
    return (getTransaction().isText() && (UTF.equals(getTransaction()
        .getValue(), mValue)));
  }

}
