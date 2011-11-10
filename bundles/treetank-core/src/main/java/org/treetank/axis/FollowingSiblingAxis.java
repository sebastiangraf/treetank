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

import org.treetank.api.IReadTransaction;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.IStructNode;

/**
 * <h1>FollowingSiblingAxis</h1>
 * 
 * <p>
 * Iterate over all following siblings of kind ELEMENT or TEXT starting at a given node. Self is not included.
 * </p>
 */
public class FollowingSiblingAxis extends AbsAxis {

    private boolean mIsFirst;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public FollowingSiblingAxis(final IReadTransaction rtx) {

        super(rtx);
        mIsFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        mIsFirst = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {

        if (mIsFirst) {
            mIsFirst = false;
            // if the context node is an attribute or namespace node,
            // the following-sibling axis is empty
            if (getTransaction().getNode().getKind() == ENodes.ATTRIBUTE_KIND
            // || getTransaction().isNamespaceKind()
            ) {
                resetToStartKey();
                return false;
            }
        }

        resetToLastKey();

        if (((IStructNode)getTransaction().getNode()).hasRightSibling()) {
            getTransaction().moveToRightSibling();
            return true;
        }
        resetToStartKey();
        return false;
    }

}
