/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.api;

import java.util.Iterator;

/**
 * <h1>IAxis</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to iterate over nodes according to a given axis step without storing intermediate lists.
 * <code>IAxis</code> extends the well-known Java <code>Iterator&lt;Long&gt;</code> and
 * <code>Iterable&lt;Long&gt;</code> interfaces
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 * <ol>
 * <li><strong>Precondition</strong> before first call to <code>IAxis.hasNext()</code>:
 * <code>IReadTransaction.getNodeKey() == n</code> .</li>
 * <li><code>IAxis.next()</code> must be called exactly once after <code>IAxis.hasNext() == true</code>.</li>
 * <li><code>IReadTransaction.getNodeKey()</code> must be equal right after <code>IAxis.hasNext()</code> and
 * right before the next call to <code>IAxis.hasNext()</code>.</li>
 * <li>If used with <code>IWriteTransaction</code>, there are no modification during an enhanced for loop.
 * </li>
 * <li><strong>Postcondition</strong> after <code>IAxis.hasNext() == false</code>:
 * <code>IReadTransaction.getNodeKey() == n</code>.</li>
 * </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 * 
 * <pre>
 *   ...
 *   for (final long key : new ExampleAxis(rtx)) {
 *      ...
 *   }
 *   ...
 * </pre>
 * 
 * <pre>
 *   ...
 *   final IAxis axis = new ExampleAxis(rtx);
 *   while (axis.hasNext()) {
 *     // Move transaction cursor to do something.
 *     axis.next();
 *     System.out.println(rtx.getLocalPart());
 *   }
 *   ...
 * </pre>
 * 
 * <pre>
 *   ...
 *   final IAxis axis = new ExampleAxis(rtx);
 *   long count = 0L;
 *   while (axis.hasNext()) {
 *     // Only count, do nothing with the transaction cursor.
 *     count += 1;
 *   }
 *   ...
 * </pre>
 * 
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // Must extend &lt;code&gt;AbstractAxis&lt;/code&gt; and implement &lt;code&gt;IAxis&lt;/code&gt;.
 * public class ExampleAxis extends AbstractAxis implements IAxis {
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
 *         // Must be called as first.
 *         resetToLastKey();
 *         if (getTransaction.hasParent) {
 *             // Leave cursor in checked state before returning true.
 *             getTransaction().moveToParent();
 *             return true;
 *         } else {
 *             // Must be called before returning false.
 *             resetToStartKey();
 *             return false;
 *         }
 *     }
 * 
 * }
 * </pre>
 * 
 * </p>
 */
public interface IAxis extends Iterator<Long>, Iterable<Long> {

    /**
     * Reset axis to new start node key. Used for nesting.
     * 
     * @param mNodeKey
     *            New start node key.
     */
    void reset(final long mNodeKey);

    /**
     * Access transaction to which this axis is bound.
     * 
     * @return Transaction to which this axis is bound.
     */
    IReadTransaction getTransaction();

}
