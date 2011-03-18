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

package org.treetank.service.xml.xpath.expr;

import java.util.Arrays;
import java.util.List;

import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;

/**
 * <h1>SequenceAxis</h1>
 * <p>
 * Axis that represents a sequence of singleExpressions, normally separated by a ','.
 * </p>
 * <p>
 * Calling hasNext() returns the results of the singleExpressions consecutively.
 * </p>
 * 
 */
public class SequenceAxis extends AbsAxis {

    private final List<AbsAxis> mSeq;
    private AbsAxis mCurrent;
    private int mNum;

    /**
     * 
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param axis
     *            The singleExpressions contained by the sequence
     */
    public SequenceAxis(final IReadTransaction rtx, final AbsAxis... axis) {

        super(rtx);
        mSeq = Arrays.asList(axis);
        mNum = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        if (mSeq != null) {
            for (AbsAxis ax : mSeq) {
                ax.reset(mNodeKey);
            }
        }
        mCurrent = null;
        mNum = 0;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mCurrent != null) {

            if (mCurrent.hasNext()) {
                return true;
            } else {
                // necessary, because previous hasNext() changes state
                resetToLastKey();
            }
        }

        while (mNum < mSeq.size()) {

            mCurrent = mSeq.get(mNum++);

            // mCurrent.getTransaction().moveTo(getTransaction().getNodeKey());
            mCurrent.reset(getTransaction().getNode().getNodeKey());
            if (mCurrent.hasNext()) {
                return true;
            }
        }

        resetToStartKey();
        return false;

    }

}
