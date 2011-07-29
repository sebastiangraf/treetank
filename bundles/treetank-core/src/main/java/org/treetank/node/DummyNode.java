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

package org.treetank.node;

import java.util.Arrays;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.settings.EFixed;

/**
 * Dummy node.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DummyNode extends AbsStructNode {

    /**
     * Creating a dummy node.
     * 
     * @param paramByteBuilder
     *            byte array with data
     * @param paramPointerBuilder
     *            byte array with data
     */
    public DummyNode(final byte[] paramByteBuilder, final byte[] paramPointerBuilder) {
        super(paramByteBuilder, paramPointerBuilder);
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Arrays.hashCode(mByteData);
        return result;
    }

    /** Cloning node not supported. */
    @Override
    public AbsNode clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getFirstChildKey() {
        return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public void setFirstChildKey(final long mFirstChildKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementChildCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementChildCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChildCount(final long paramChildCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create node data.
     * 
     * @param paramNodeKey
     *            node key
     * @param paramParentKey
     *            parent node key
     * @return {@link DummyNode} instance
     */
    public static DummyNode createData(final long paramNodeKey, final long paramParentKey) {
        final byte[] byteData = new byte[ENodes.DUMMY_KIND.getByteSize()];
        final byte[] pointerData = new byte[ENodes.DUMMY_KIND.getPointerSize()];

        int mCount = AbsNode.NODE_KEY;
        for (byte aByte : longToByteArray(paramNodeKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.PARENT_KEY;
        for (byte aByte : longToByteArray(paramParentKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.LEFT_SIBLING_KEY;
        for (byte aByte : longToByteArray((Long)EFixed.NULL_NODE_KEY.getStandardProperty())) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.RIGHT_SIBLING_KEY;
        for (byte aByte : longToByteArray((Long)EFixed.NULL_NODE_KEY.getStandardProperty())) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.FIRST_CHILD_KEY;
        for (byte aByte : longToByteArray((Long)EFixed.NULL_NODE_KEY.getStandardProperty())) {
            pointerData[mCount++] = aByte;
        }
        return new DummyNode(byteData, pointerData);
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }

}
