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
package org.treetank.access;

import org.treetank.annotations.NotNull;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ElementNode;
import org.treetank.node.TextNode;

/**
 * Allows insertion of subtrees.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
class InsertSubtreeVisitor extends AbsVisitorSupport {
    /**
     * Read-transaction which implements the {@link IReadTransaction} interface.
     */
    private final IReadTransaction mRtx;

    /**
     * Write-transaction which implements the {@link IWriteTransaction} interface.
     */
    private final IWriteTransaction mWtx;

    /** Determines how to insert a node. */
    private EInsert mInsert;

    /**
     * Constructor.
     * 
     * @param paramRtx
     *            read-transaction which implements the {@link IReadTransaction} interface
     * @param paramWtx
     *            write-transaction which implements the {@link IWriteTransaction} interface
     * @param paramInsert
     *            determines how to insert a node
     */
    InsertSubtreeVisitor(@NotNull final IReadTransaction paramRtx, @NotNull final IWriteTransaction paramWtx,
        @NotNull final EInsert paramInsert) {
        assert paramRtx != null;
        assert paramWtx != null;
        assert paramInsert != null;
        mRtx = paramRtx;
        mWtx = paramWtx;
        mInsert = paramInsert;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final ElementNode paramNode) {
        mRtx.moveTo(paramNode.getNodeKey());
        try {
            mInsert.insertNode(mWtx, mRtx);

            if (paramNode.getNamespaceCount() > 0) {
                mInsert = EInsert.ASNONSTRUCTURAL;
                for (int i = 0; i < paramNode.getNamespaceCount(); i++) {
                    mRtx.moveToNamespace(i);
                    mInsert.insertNode(mWtx, mRtx);
                    mRtx.moveToParent();
                }
            }

            if (paramNode.getAttributeCount() > 0) {
                mInsert = EInsert.ASNONSTRUCTURAL;
                for (int i = 0; i < paramNode.getAttributeCount(); i++) {
                    mRtx.moveToAttribute(i);
                    mInsert.insertNode(mWtx, mRtx);
                    mRtx.moveToParent();
                }
            }

            if (paramNode.hasFirstChild()) {
                mInsert = EInsert.ASFIRSTCHILD;
                mRtx.moveToFirstChild();
            } else if (paramNode.hasRightSibling()) {
                mInsert = EInsert.ASRIGHTSIBLING;
                mRtx.moveToRightSibling();
            }
        } catch (final AbsTTException e) {
            e.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final TextNode paramNode) {
        mRtx.moveTo(paramNode.getNodeKey());
        try {
            mInsert.insertNode(mWtx, mRtx);
        } catch (final AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
