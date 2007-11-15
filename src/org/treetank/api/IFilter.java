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

package org.treetank.api;

/**
 * <h1>IAxis</h1>
 * 
 * <p>
 * Interface to iterate over the TreeTank according to an iteration logic.
 * All implementations must comply with the following:
 * <li>next() must be called exactly once after hasNext() yields true.</li>
 * <li>after hasNext() is false, the transaction points to the node where
 *     it started</li>
 * <li>before each hasNext(), the cursor is guaranteed to point to the last
 *     node found with hasNext().</li>
 * </p>
 * <p>
 * This behavior can be achieved by:
 * <li>Always call super.hasNext() as the first thing in hasNext().</li>
 * <li>Always call reset() before return false in hasNext().</li> 
 * </p>
 */
public interface IFilter {

  /**
   * Access transaction to which this axis is bound.
   * 
   * @return Transaction to which this axis is bound.
   */
  public boolean test(final IReadTransaction rtx);

}
