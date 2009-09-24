/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: IFilter.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.api;

/**
 * <h1>IFilter</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Filter the node currently selected by the provided transaction.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 * <ol>
 * <li><strong>Precondition</strong> before each call to
 * <code>IFilter.filter()</code>:
 * <code>IReadTransaction.getNodeKey() == n</code>.</li>
 * <li><strong>Postcondition</strong> after each call to
 * <code>IFilter.filter()</code>:
 * <code>IReadTransaction.getNodeKey() == n</code>.</li>
 * </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // hasNext() yields true iff rtx selects an element with local part &quot;foo&quot;.
 * new FilterAxis(new SelfAxis(rtx), new NameFilter(rtx, &quot;foo&quot;));
 * </pre>
 * 
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // Must extend &lt;code&gt;AbstractFilter&lt;/code&gt; and implement &lt;code&gt;IFilter&lt;/code&gt;.
 * public class ExampleFilter extends AbstractFilter implements IFilter {
 * 
 * 	public ExampleFilter(final IReadTransaction rtx) {
 * 		// Must be called as first.
 * 		super(rtx);
 * 	}
 * 
 * 	public final boolean filter() {
 * 		// Do not move cursor.
 * 		return (getTransaction().isSelected());
 * 	}
 * }
 * </pre>
 * 
 * </p>
 */
public interface IFilter {

	/**
	 * Access transaction to which this filter is bound.
	 * 
	 * @return Transaction to which this filter is bound.
	 */
	IReadTransaction getTransaction();

	/**
	 * Apply filter on current node of transaction.
	 * 
	 * @return True if node passes filter. False else.
	 */
	boolean filter();

}
