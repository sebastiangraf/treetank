/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.axis;

import org.treetank.api.INodeReadTransaction;
import org.treetank.node.ENode;
import static org.treetank.access.NodeReadTransaction.ROOT_NODE;

/**
 * <h1>AncestorAxis</h1>
 * 
 * <p>
 * Iterate over all descendants of kind ELEMENT or TEXT starting at a given node. Self is not included.
 * </p>
 */
public class AncestorAxis extends AbsAxis {

    /**
     * First touch of node.
     */
    private boolean mFirst;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public AncestorAxis(final INodeReadTransaction rtx) {
        super(rtx);
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mIncludeSelf
     *            Is self included?
     */
    public AncestorAxis(final INodeReadTransaction rtx, final boolean mIncludeSelf) {
        super(rtx, mIncludeSelf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        mFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {
        resetToLastKey();

        // Self
        if (mFirst && isSelfIncluded()) {
            mFirst = false;
            return true;
        }

        if (getTransaction().getNode().getKind() != ENode.ROOT_KIND
            && getTransaction().getNode().hasParent()
            && getTransaction().getNode().getParentKey() != ROOT_NODE) {
            getTransaction().moveToParent();
            return true;
        }
        resetToStartKey();
        return false;
    }

}
