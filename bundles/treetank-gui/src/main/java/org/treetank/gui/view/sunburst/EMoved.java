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

package org.treetank.gui.view.sunburst;

import java.util.Stack;

import org.treetank.api.IReadTransaction;
import org.treetank.node.AbsStructNode;

/**
 * Determines movement of transaction and updates stacks accordingly.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EMoved {

    /** Start of traversal or cursor moved to a right sibling of a node. */
    STARTRIGHTSIBL {
        /**
         * {@inheritDoc}
         */
        @Override
        public void processMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramParentStack, final Stack<Integer> paramDescendantsStack) {
            // Do nothing.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void processCompareMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramDescendants, final Stack<Integer> paramParentStack,
            final Stack<Integer> paramModificationStack) {
            // Do nothing.
        }
    },

    /** Next node is a child of the current node. */
    CHILD {
        /**
         * {@inheritDoc}
         */
        @Override
        public void processMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramParentStack, final Stack<Integer> paramDescendantsStack) {
            assert !paramAngleStack.empty();
            paramItem.mAngle = paramAngleStack.peek();
            assert !paramExtensionStack.empty();
            paramItem.mExtension = paramExtensionStack.peek();
            assert !paramParentStack.empty();
            paramItem.mIndexToParent = paramParentStack.peek();
            assert !paramDescendantsStack.empty();
            paramItem.mParentDescendantCount = paramDescendantsStack.peek();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void processCompareMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramDescendantsStack, final Stack<Integer> paramParentStack,
            final Stack<Integer> paramModificationStack) {
            assert !paramAngleStack.empty();
            paramItem.mAngle = paramAngleStack.peek();
            assert !paramExtensionStack.empty();
            paramItem.mExtension = paramExtensionStack.peek();
            assert !paramDescendantsStack.empty();
            paramItem.mParentDescendantCount = paramDescendantsStack.peek();
            assert !paramParentStack.empty();
            paramItem.mIndexToParent = paramParentStack.peek();
            assert !paramModificationStack.empty();
            paramItem.mParentModificationCount = paramModificationStack.peek();
        }
    },

    /** Next node is the rightsibling of the first anchestor node which has one. */
    ANCHESTSIBL {
        /**
         * {@inheritDoc}
         */
        @Override
        public void processMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramParentStack, final Stack<Integer> paramDescendantsStack) {
            assert !paramAngleStack.empty();
            paramItem.mAngle = paramAngleStack.pop();
            assert !paramExtensionStack.empty();
            paramItem.mAngle += paramExtensionStack.pop();
            assert !paramExtensionStack.empty();
            paramItem.mExtension = paramExtensionStack.peek();
            assert !paramParentStack.empty();
            paramParentStack.pop();
            assert !paramParentStack.empty();
            paramItem.mIndexToParent = paramParentStack.peek();
            assert !paramDescendantsStack.empty();
            paramDescendantsStack.pop();
            assert !paramDescendantsStack.empty();
            paramItem.mParentDescendantCount = paramDescendantsStack.peek();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void processCompareMove(final IReadTransaction paramRtx, final Item paramItem,
            final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
            final Stack<Integer> paramDescendantsStack, final Stack<Integer> paramParentStack,
            final Stack<Integer> paramModificationStack) {
            assert !paramAngleStack.empty();
            paramItem.mAngle = paramAngleStack.pop();
            assert !paramExtensionStack.empty();
            paramItem.mAngle += paramExtensionStack.pop();
            assert !paramExtensionStack.empty();
            paramItem.mExtension = paramExtensionStack.peek();
            assert !paramParentStack.empty();
            paramParentStack.pop();
            assert !paramParentStack.empty();
            paramItem.mIndexToParent = paramParentStack.peek();
            assert !paramDescendantsStack.empty();
            paramDescendantsStack.pop();
            assert !paramDescendantsStack.empty();
            paramItem.mParentDescendantCount = paramDescendantsStack.peek();
            assert !paramModificationStack.empty();
            paramModificationStack.pop();
            assert !paramModificationStack.empty();
            paramItem.mParentModificationCount = paramModificationStack.peek();
        }
    };

    /**
     * Process movement of Treetank {@link IReadTransaction}.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     * @param paramItem
     *            Item which does hold sunburst item angles and extensions
     * @param paramAngleStack
     *            stack for angles
     * @param paramExtensionStack
     *            stack for extensions
     * @param paramParentStack
     *            stack for parent indexes
     * @param paramDescendantsStack
     *            stack for descendants
     */
    public abstract void processMove(final IReadTransaction paramRtx, final Item paramItem,
        final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
        final Stack<Integer> paramParentStack, final Stack<Integer> paramDescendantsStack);

    /**
     * Process movement of Treetank {@link IReadTransaction}, while comparing revisions.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     * @param paramItem
     *            Item which does hold sunburst item angles and extensions
     * @param paramAngleStack
     *            stack for angles
     * @param paramExtensionStack
     *            stack for extensions
     * @param paramDescendantsStack
     *            stack for descendants
     * @param paramParentStack
     *            stack for parent indexes
     * @param paramModificationStack
     *            stack for modifications
     */
    public abstract void processCompareMove(final IReadTransaction paramRtx, final Item paramItem,
        final Stack<Float> paramAngleStack, final Stack<Float> paramExtensionStack,
        final Stack<Integer> paramDescendantsStack, final Stack<Integer> paramParentStack,
        final Stack<Integer> paramModificationStack);
}
