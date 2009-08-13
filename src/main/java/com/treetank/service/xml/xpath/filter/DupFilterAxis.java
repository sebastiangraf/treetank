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
 * $Id: DupFilterAxis.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import java.util.HashSet;
import java.util.Set;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.NestedAxis;
import com.treetank.service.xml.xpath.expr.UnionAxis;

/**
 * <h1>DupFilterAxis</h1>
 * <p>
 * Duplicate Filter. Assures that the resulting node set contains no duplicates.
 * </p>
 * <p>
 * Encapsulates a given XPath axis and only passes on those items that have not
 * already been passed. This does not break the pipeline since every
 * intermediary result is immediately passed on, as long as it is not already in
 * the set (which indicates that it was already returned).
 * </p>
 */
public class DupFilterAxis extends AbstractAxis {

	/** Sequence that may contain duplicates. */
	private final IAxis mAxis;

	/** Set that stores all already returned item keys. */
	private final Set<Long> mDupSet;

	/**
	 * Defines whether next() has to be called for the dupAxis after calling
	 * hasNext(). In some cases next() has already been called by another axis.
	 */
	private final boolean callNext;

	/**
	 * Constructor. Initializes the internal state.
	 * 
	 * @param rtx
	 *            Exclusive (immutable) trx to iterate with.
	 * @param dupAxis
	 *            Sequence that may return duplicates.
	 */
	public DupFilterAxis(final IReadTransaction rtx, final IAxis dupAxis) {

		super(rtx);
		mAxis = dupAxis;
		mDupSet = new HashSet<Long>();
		// if the dupAxis is not one of the specified axis, 'next()' has
		// explicitly
		// be called for those axis after calling 'hasNext()'. For all other
		// axis
		// next() has already been called by another axis.
		callNext = !(mAxis instanceof FilterAxis || mAxis instanceof NestedAxis || mAxis instanceof UnionAxis);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void reset(final long nodeKey) {

		super.reset(nodeKey);
		if (mAxis != null) {
			mAxis.reset(nodeKey);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean hasNext() {

		resetToLastKey();

		while (mAxis.hasNext()) {

			// call next(), if it was not already called for that axis.
			if (callNext) {
				mAxis.next();
			}

			// add current item key to the set. If true is returned the item is
			// no
			// duplicate and can be returned by the duplicate filter.
			if (mDupSet.add(getTransaction().getNode().getNodeKey())) {
				return true;
			}
		}

		resetToStartKey();
		return false;
	}
}