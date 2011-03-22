/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.node;

import java.util.Arrays;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.settings.EFixed;

/**
 * <h1>DocumentNode</h1>
 * 
 * <p>
 * Node representing the root of a document. This node is guaranteed to exist in revision 0 and can not be
 * removed.
 * </p>
 */
public final class DocumentRootNode extends AbsStructNode {

    /**
     * Constructor.
     * 
     * @param mLongBuilder
     *            long array to set
     * @param mIntBuilder
     *            int array to set
     */
    DocumentRootNode(final long[] mLongBuilder, final int[] mIntBuilder) {
        super(mLongBuilder, mIntBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.ROOT_KIND;
    }

    @Override
    public void setLeftSiblingKey(final long mLeftSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRightSiblingKey(final long rightSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbsNode clone() {
        return new DocumentRootNode(ENodes.cloneData(mLongData), ENodes.cloneData(mIntData));
    }

    public static DocumentRootNode createData() {
        final long[] longData = new long[ENodes.ROOT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ROOT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = (Long)EFixed.ROOT_NODE_KEY.getStandardProperty();
        longData[AbsNode.PARENT_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.CHILD_COUNT] = 0;
        longData[AbsStructNode.FIRST_CHILD_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.LEFT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        return new DocumentRootNode(longData, intData);
    }

    @Override
    public int hashCode() {
        final int prime = 35573;
        int result = 1;
        result = prime * result + Arrays.hashCode(mLongData);
        result = prime * result + Arrays.hashCode(mIntData);
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IItem> T accept(final IReadTransaction paramTransaction) {
        return (T)paramTransaction.getNode(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }
}
