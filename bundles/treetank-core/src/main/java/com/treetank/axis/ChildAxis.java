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
 * $Id: ChildAxis.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.axis;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AbsStructNode;

/**
 * <h1>ChildAxis</h1>
 * 
 * <p>
 * Iterate over all children of kind ELEMENT or TEXT starting at a given node.
 * Self is not included.
 * </p>
 */
public class ChildAxis extends AbstractAxis implements IAxis {

    /** Has another child node. */
    private boolean mFirst;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public ChildAxis(final IReadTransaction rtx) {
        super(rtx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long nodeKey) {
        super.reset(nodeKey);
        mFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean hasNext() {
        resetToLastKey();
        if (!mFirst
                && ((AbsStructNode) getTransaction().getNode())
                        .hasRightSibling()) {
            getTransaction().moveToRightSibling();
            return true;
        } else if (mFirst
                && ((AbsStructNode) getTransaction().getNode()).hasFirstChild()) {
            mFirst = false;
            getTransaction().moveToFirstChild();
            return true;
        } else {
            resetToStartKey();
            return false;
        }
    }

}
