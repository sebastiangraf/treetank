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

import java.util.Iterator;

/**
 * <h1>IAxis</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to iterate over nodes without storing intermediate lists.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 *  <ol>
 *   <li><strong>Precondition</strong> before first call to
 *       <code>IAxis.hasNext()</code>:
 *       <code>IReadTransaction.isSelected() == true
 *       && IReadTransaction.getNodeKey() == n</code>.</li>
 *   <li><code>IAxis.next()</code> must be called exactly once after
 *       <code>IAxis.hasNext() == true</code>.</li>
 *   <li><code>IReadTransaction.getNodeKey()</code> must be equal right after
 *       <code>IAxis.hasNext()</code> and right before the next call to
 *       <code>IAxis.hasNext()</code>.</li>
 *   <li>If used with <code>IWriteTransaction</code>, there are no modification
 *       during an enhanced for loop.</li>
 *   <li><strong>Postcondition</strong> after 
 *       <code>IAxis.hasNext() == false</code>:
 *       <code>IReadTransaction.isSelected() == true &&
 *       IReadTransaction.getNodeKey() == n</code>.</li>
 *  </ol>
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <p>
 *  <pre>
 *   public class ExampleAxis implements IAxis {
 *  
 *     public ExampleAxis(final IReadTransaction rtx) {
 *       // Must be called as first.
 *       super(rtx);
 *       // Some code moving rtx.
 *       ...
 *       // Must reset rtx to start key.
 *       resetToStartKey();
 *     }
 *
 *     public final boolean hasNext() {
 *       // Must be called as first.
 *       resetToLastKey();
 *       if (getTransaction.hasParent) {
 *         // Leave cursor in checked state before returning true.
 *         getTransaction().moveToParent();
 *         return true;
 *       } else {
 *         // Must be called before returning false.
 *         resetToStartKey();
 *         return false;
 *       }
 *     }
 *   
 *   }
 *   </pre>
 *   <pre>
 *   ...
 *   for (final long key : new ExampleAxis(rtx)) {
 *      ...
 *   }
 *   ...
 *  </pre>
 * </p>
 */
public interface IAxis extends Iterator<Long>, Iterable<Long> {

  /**
   * Reset axis to new start node key. Used for nesting.
   * 
   * @param nodeKey New start node key.
   */
  public void reset(final long nodeKey);

  /**
   * Access transaction to which this axis is bound.
   * 
   * @return Transaction to which this axis is bound.
   */
  public IReadTransaction getTransaction();

}
