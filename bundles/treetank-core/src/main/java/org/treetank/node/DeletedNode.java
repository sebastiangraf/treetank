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

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode extends AbsNode {

    DeletedNode(final long[] mLongBuilder, final int[] mIntBuilder) {
        super(mLongBuilder, mIntBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        super.serialize(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.DELETE_KIND;
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new DeletedNode(ENodes.cloneData(mLongData), ENodes.cloneData(mIntData));
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long parentKey) {
        final long[] longData = new long[ENodes.DELETE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.DELETE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = mNodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        return new DeletedNode(longData, intData);
    }

    @Override
    public int hashCode() {
        return 0;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IItem> T accept(final IReadTransaction paramTransaction) {
        return (T)paramTransaction.getNode(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        // Do nothing.
    }
}
