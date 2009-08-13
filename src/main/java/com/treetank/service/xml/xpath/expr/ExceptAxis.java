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
 * $Id: ExceptAxis.java 4248 2008-07-08 08:57:57Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import java.util.HashSet;
import java.util.Set;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractAxis;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;

/**
 * <h1>ExceptAxis</h1>
 * <p>
 * Returns the nodes of the first operand except those of the second operand.
 * This axis takes two node sequences as operands and returns a sequence
 * containing all the nodes that occur in the first, but not in the second
 * operand.
 * </p>
 */
public class ExceptAxis extends AbstractAxis implements IAxis {

	/** First operand sequence. */
	private final IAxis mOp1;

	/** Second operand sequence. */
	private final IAxis mOp2;

	/**
	 * Set that is used to determine, whether an item of the first operand is
	 * also contained in the result set of the second operand.
	 */
	private final Set<Long> mDupSet;

	/**
	 * Constructor. Initializes the internal state.
	 * 
	 * @param rtx
	 *            Exclusive (immutable) trx to iterate with.
	 * @param operand1
	 *            First operand
	 * @param operand2
	 *            Second operand
	 */
	public ExceptAxis(final IReadTransaction rtx, final IAxis operand1,
			final IAxis operand2) {

		super(rtx);
		mOp1 = operand1;
		mOp2 = operand2;
		mDupSet = new HashSet<Long>();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset(final long nodeKey) {

		super.reset(nodeKey);
		if (mDupSet != null) {
			mDupSet.clear();
		}

		if (mOp1 != null) {
			mOp1.reset(nodeKey);
		}
		if (mOp2 != null) {
			mOp2.reset(nodeKey);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {

		// first all items of the second operand are stored in the set.
		while (mOp2.hasNext()) {
			if (getTransaction().getNode().getNodeKey() < 0) { // only nodes are
				// allowed
				throw new XPathError(ErrorType.XPTY0004);
			}
			mDupSet.add(getTransaction().getNode().getNodeKey());
		}

		while (mOp1.hasNext()) {
			if (getTransaction().getNode().getNodeKey() < 0) { // only nodes are
				// allowed
				throw new XPathError(ErrorType.XPTY0004);
			}

			// return true, if node is not already in the set, which means, that
			// it is
			// not also an item of the result set of the second operand
			// sequence.
			if (mDupSet.add(getTransaction().getNode().getNodeKey())) {
				return true;
			}
		}

		return false;
	}

}
